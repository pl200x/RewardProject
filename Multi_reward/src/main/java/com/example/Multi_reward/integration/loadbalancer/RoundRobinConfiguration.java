package com.example.Multi_reward.integration.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Loaded only in the named LoadBalancer child context; intentionally not component-scanned. */
public class RoundRobinConfiguration {
    @Bean
    ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
            Environment environment, LoadBalancerClientFactory clientFactory) {
        String serviceId = environment.getRequiredProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(
                clientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class), serviceId);
    }
}
