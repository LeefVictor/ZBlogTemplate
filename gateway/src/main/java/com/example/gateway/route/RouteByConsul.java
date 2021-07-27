package com.example.gateway.route;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import com.example.gateway.route.Reload.ReloadBuilder;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.gateway.filter.GroupWeightConfig;
import org.springframework.cloud.gateway.filter.RouteDelEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


/**
 * 配置监听器
 */
@Component
public class RouteByConsul {

    private Logger log = LoggerFactory.getLogger(RouteByConsul.class);

    private final String ROUTE_FOLDER = "route/gateway/";

    //配置是否被修改的检查， 避免频繁刷新
    private final Map<String, Long> changeCheck = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;

    private final ConsulDiscoveryClient discoveryClient;

    public final ApplicationEventPublisher publisher;

    @Value("${spring.cloud.consul.host}")
    private String consulHost;

    @Value("${server.port}")
    private int localServerPort;

    @Value("${spring.cloud.consul.discovery.acl-token}")
    private String aclToken;

    public RouteByConsul(RestTemplate restTemplate,
        ConsulDiscoveryClient discoveryClient, ApplicationEventPublisher publisher) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.publisher = publisher;
    }

    /**
     * 定时获取配置， 并对比注册中心发现， 观察实例是否下线，下线就删掉路由
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void fetchAndReload() {
        List<String> service = Optional.ofNullable(discoveryClient.getServices())
            .orElse(Collections.emptyList())
            .stream()
            .filter(f -> f.startsWith("serv-")).collect(Collectors.toList());

        List<ServiceInstance> del = new ArrayList<>();


        for (String s : service) {
            List<ServiceInstance> instances = discoveryClient.getInstances(s);
            for (ServiceInstance instance : instances) {
                boolean invalidate = false;
                for (Check check : ((ConsulServiceInstance) instance).getHealthService().getChecks()) {
                    if (check.getStatus() != CheckStatus.PASSING) {
                        invalidate = true;break;
                    }
                }
                if (!invalidate) {
                    reload(instance.getInstanceId(), instance.getHost(), instance.getPort());
                } else {
                    if (changeCheck.remove(instance.getInstanceId()) != null) {
                        del.add(instance);
                    }
                }
            }
        }

        for (ServiceInstance s : del) {
            log.info("移除 {} 路由配置", s.getInstanceId());
            ReloadBuilder.aReload()
                .withLocalServerPort(localServerPort)
                .withRestTemplate(restTemplate)
                .withInstanceId(s.getInstanceId())
                .build()
                .del().refresh();
            publisher.publishEvent(new RouteDelEvent(1, s.getInstanceId()));
        }

    }

    /**
     * 根据配置进行路由重载
     * @param instanceId
     * @param host
     * @param port
     */
    protected void reload(String instanceId, String host, int port) {
        KVResult config = getConfig(instanceId);
        if (!changeCheck.containsKey(instanceId) || !changeCheck.get(instanceId).equals(config.getModifyIndex())) {
            changeCheck.put(instanceId, config.getModifyIndex());

            ReloadBuilder builder = ReloadBuilder.aReload().withLocalServerPort(localServerPort)
                .withInstanceId(instanceId).withServicePort(port)
                .withServiceHost(host)
                .withRestTemplate(restTemplate);
            String[] splits = config.getValue().split("\n");
            for (String split : splits) {
                if (split.startsWith("perdicates.path=")) {
                    builder.withPath(split.split("=")[1]);
                }
                if (split.startsWith("filters.rateLimit.enable=")) {
                    builder.withRateLimitEnable(Boolean.parseBoolean(split.split("=")[1]));
                }
                if (split.startsWith("filters.rateLimit.burstCapacity=")) {
                    builder.withRateLimitBurst(Integer.parseInt(split.split("=")[1]));
                }
                if (split.startsWith("filters.rateLimit.replenishRate=")) {
                    builder.withRateLimitReplen(Integer.parseInt(split.split("=")[1]));
                }
                if (split.startsWith("filters.stripPrefix=")) {
                    builder.withStripPathValue(Integer.parseInt(split.split("=")[1]));
                }
                if (split.startsWith("perdicates.weight.group=")) {
                    builder.withWeightGroup(split.split("=")[1]);
                }
                if (split.startsWith("perdicates.weight.value=")) {
                    builder.withWeightValue(Integer.valueOf(split.split("=")[1]));
                }
            }

            builder.build().post().refresh();
            log.info("更新 {} 路由配置", instanceId);
        }
    }


    private KVResult getConfig(String instanceId) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + aclToken);
        HttpEntity<String> httpEntity = new HttpEntity(headers);

        String url = "http://" + consulHost + ":8500/v1/kv/" + ROUTE_FOLDER + instanceId;
        log.info("获取路由配置 {}", url);
        ResponseEntity<String> resp = null;
        try {
            resp = restTemplate.exchange(url,
                HttpMethod.GET,
                httpEntity,
                String.class);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            log.error("error", e);
        }
        List<KVResult> obj = new Gson().fromJson(resp.getBody(), new TypeToken<List<KVResult>>() {}.getType());
        transWithBase64(obj);
        assert obj.size() == 1;
        return obj.get(0);
    }

    //直接通过接口获取的配置都是base64加密的值
    private void transWithBase64(List<KVResult> kvResults) {
        for (KVResult kvResult : kvResults) {
            kvResult.setValue(new String(Base64Utils.decodeFromString(kvResult.getValue()), StandardCharsets.UTF_8));
        }
    }


}
