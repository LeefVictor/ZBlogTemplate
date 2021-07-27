package com.example.gateway.route;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Reload {

    private final String POST_JSON = ""
        + "{   "
        + "    \"predicates\":["
        + "        {"
        + "            \"name\":\"Weight\",  "
        + "            \"args\":{"
        + "                \"_genkey_0\":\"{GROUP_NAME}\", "
        + "                \"_genkey_1\":\"{WEIGHT_VALUE}\"  "
        + "            }"
        + "        },"
        + "        {"
        + "            \"name\":\"Path\","
        + "            \"args\":{"
        + "                \"_genkey_0\":\"{PATH}\"  "
        + "            }"
        + "        }"
        + "    ],"
        + "    \"filters\":["
        + "        {"
        + "            \"name\":\"StripPrefix\", "
        + "            \"args\":{"
        + "                \"_genkey_0\":\"{STRIP_VALUE}\" "
        + "            }"
        + "        }{RATELIMIT}"
        + "    ],"
        + "    \"uri\":\"http://{SERVICE_HOST}:{SERVICE_PORT}\", "
        + "    \"order\":0 "
        + "}";

    private final String RATELIMIT_CONF = "{"
        + "            \"name\":\"RequestRateLimiter\","
        + "            \"args\":{"
        + "                \"redis-rate-limiter.replenishRate\":{REPLEN_VALUE},"
        + "                \"redis-rate-limiter.burstCapacity\":{BURST_VALUE},"
        + "                \"key-resolver\":\"#{@userKeyResolver}\"" // 这个key自行提供多个组合方式，然后让各个服务自选，这里就直接写死了
        + "            }"
        + "        }";

    private String instanceId;
    private String serviceHost;
    private int servicePort;
    private int weightValue;
    private String weightGroup;
    private String path;
    private int stripPathValue;
    private boolean rateLimitEnable;
    private int rateLimitReplen;
    private int rateLimitBurst;
    private int localServerPort;
    private RestTemplate restTemplate;


    public Reload post() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String body = POST_JSON;
        if (rateLimitEnable) {
            String ratelimitStr = RATELIMIT_CONF;
            ratelimitStr = ratelimitStr.replace("{REPLEN_VALUE}", String.valueOf(rateLimitReplen))
                .replace("{BURST_VALUE}", String.valueOf(rateLimitBurst));
            body = body.replace("{RATELIMIT}", "," + ratelimitStr);
        } else {
            body = body.replace("{RATELIMIT}", "");
        }
        body = body.replace("{GROUP_NAME}", weightGroup)
            .replace("{WEIGHT_VALUE}", String.valueOf(weightValue))
            .replace("{PATH}", path)
            .replace("{STRIP_VALUE}", String.valueOf(stripPathValue))
            .replace("{SERVICE_HOST}", serviceHost)
            .replace("{SERVICE_PORT}", String.valueOf(servicePort));

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url(), entity, String.class);
        assert responseEntity.getStatusCodeValue() == 201 || responseEntity.getStatusCodeValue() == 200;
        return this;
    }


    public Reload del() {
        restTemplate.delete(url());
        return this;
    }


    public Reload refresh() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            "http://localhost:" + localServerPort + "/actuator/gateway/refresh",
            entity,
            String.class);
        assert responseEntity.getStatusCodeValue() == 200;
        return this;
    }

    private String url() {
        return "http://localhost:" + localServerPort + "/actuator/gateway/routes/" + instanceId;
    }


    public static final class ReloadBuilder {

        private String instanceId;
        private String serviceHost;
        private int servicePort;
        private int weightValue;
        private String weightGroup;
        private String path;
        private int stripPathValue;
        private boolean rateLimitEnable;
        private int rateLimitReplen;
        private int rateLimitBurst;
        private int localServerPort;
        private RestTemplate restTemplate;

        private ReloadBuilder() {}

        public static ReloadBuilder aReload() { return new ReloadBuilder(); }

        public ReloadBuilder withInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public ReloadBuilder withServiceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public ReloadBuilder withServicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public ReloadBuilder withWeightValue(int weightValue) {
            this.weightValue = weightValue;
            return this;
        }

        public ReloadBuilder withWeightGroup(String weightGroup) {
            this.weightGroup = weightGroup;
            return this;
        }

        public ReloadBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public ReloadBuilder withStripPathValue(int stripPathValue) {
            this.stripPathValue = stripPathValue;
            return this;
        }

        public ReloadBuilder withRateLimitEnable(boolean rateLimitEnable) {
            this.rateLimitEnable = rateLimitEnable;
            return this;
        }

        public ReloadBuilder withRateLimitReplen(int rateLimitReplen) {
            this.rateLimitReplen = rateLimitReplen;
            return this;
        }

        public ReloadBuilder withRateLimitBurst(int rateLimitBurst) {
            this.rateLimitBurst = rateLimitBurst;
            return this;
        }

        public ReloadBuilder withLocalServerPort(int localServerPort) {
            this.localServerPort = localServerPort;
            return this;
        }

        public ReloadBuilder withRestTemplate(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
            return this;
        }

        public Reload build() {
            Reload reload = new Reload();
            reload.rateLimitBurst = this.rateLimitBurst;
            reload.path = this.path;
            reload.serviceHost = this.serviceHost;
            reload.stripPathValue = this.stripPathValue;
            reload.weightGroup = this.weightGroup;
            reload.weightValue = this.weightValue;
            reload.rateLimitReplen = this.rateLimitReplen;
            reload.servicePort = this.servicePort;
            reload.rateLimitEnable = this.rateLimitEnable;
            reload.instanceId = this.instanceId;
            reload.localServerPort = this.localServerPort;
            reload.restTemplate = this.restTemplate;
            return reload;
        }
    }
}