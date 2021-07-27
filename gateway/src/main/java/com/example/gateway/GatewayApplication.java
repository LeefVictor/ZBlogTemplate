package com.example.gateway;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.ReregistrationPredicate;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    @Bean
    public KeyResolver userKeyResolver() { // 这里是以ip+method+uri作为key值
        return exchange -> {
            String key = exchange.getRequest().getRemoteAddress().getHostName()
                .concat(exchange.getRequest().getMethodValue()).concat("_")
                .concat(exchange.getRequest().getPath().value()).concat("_");
            return Mono.just(key);
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties, ConsulDiscoveryProperties discoveryProperties,
        ConsulClient client, ReregistrationPredicate reregistrationPredicate) {
        return new TtlScheduler(heartbeatProperties, discoveryProperties, client, reregistrationPredicate);
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
