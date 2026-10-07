package com.example.Music_management.config;

import com.example.Music_management.integration.client.RewardClient;
import com.example.Music_management.integration.loadbalancer.RoundRobinConfiguration;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(clients = RewardClient.class)
@LoadBalancerClient(name = "reward-service", configuration = RoundRobinConfiguration.class)
public class FeignClientConfig {
    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    LoadBalancerFeignRequestTransformer selectedInstanceLogger(
            @Value("${eureka.instance.appname}") String service,
            @Value("${eureka.instance.instance-id}") String instance) {
        return (request, selected) -> {
            String requestId = request.headers().entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase("X-Request-ID"))
                    .flatMap(entry -> entry.getValue().stream()).findFirst().orElse("");
            log.info("event=LOAD_BALANCER_SELECTED service={} instance={} requestId={} targetService={} targetInstance={} targetHost={} targetPort={} method={} path={}",
                    service, instance, requestId, selected.getServiceId(), selected.getInstanceId(),
                    selected.getHost(), selected.getPort(), request.httpMethod(), URI.create(request.url()).getPath());
            return request;
        };
    }
}
