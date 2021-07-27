package com.example.microserv;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.ReregistrationPredicate;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class MicroServApplication {

    @Bean
    public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties, ConsulDiscoveryProperties discoveryProperties,
        ConsulClient client, ReregistrationPredicate reregistrationPredicate) {
        return new TtlScheduler(heartbeatProperties, discoveryProperties, client, reregistrationPredicate);
    }

    public static void main(String[] args) {
        SpringApplication.run(MicroServApplication.class, args);
    }

}
