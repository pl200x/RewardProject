package com.example.Music_management.integration.client;

import feign.Capability;
import feign.Client;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/** Applied only to internal Feign clients, not to external SDK HTTP clients. */
public class InternalFeignConfiguration {
    @Bean
    Retryer internalRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    Request.Options internalRequestOptions(
            @Value("${integration.http.connect-timeout-ms:1000}") int connectTimeout,
            @Value("${integration.http.read-timeout-ms:3000}") int readTimeout) {
        if (connectTimeout <= 0 || readTimeout <= 0) {
            throw new IllegalArgumentException("Internal HTTP timeouts must be positive");
        }
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS, false);
    }

    @Bean
    RequestInterceptor requestCorrelation() {
        return template -> {
            String requestId = MDC.get("traceId");
            template.header("X-Request-ID", requestId != null ? requestId : UUID.randomUUID().toString());
        };
    }

    @Bean
    Capability downstreamLogging(
            @Value("${eureka.instance.appname}") String service,
            @Value("${eureka.instance.instance-id}") String instance) {
        return new DownstreamLoggingCapability(service, instance);
    }

    /** Public because Feign invokes Capability enrichment reflectively. */
    public static final class DownstreamLoggingCapability implements Capability {
        private static final Logger log = LoggerFactory.getLogger(DownstreamLoggingCapability.class);
        private final String service;
        private final String instance;

        public DownstreamLoggingCapability(String service, String instance) {
            this.service = service;
            this.instance = instance;
        }

        @Override
        public Client enrich(Client delegate) {
            return (request, options) -> {
                URI uri = URI.create(request.url());
                String requestId = request.headers().getOrDefault("X-Request-ID", java.util.List.of(""))
                        .stream().findFirst().orElse("");
                long started = System.nanoTime();
                try {
                    var response = delegate.execute(request, options);
                    log.info("event=DOWNSTREAM_RESPONSE service={} instance={} requestId={} method={} target={} path={} status={} durationMs={}",
                            service, instance, requestId, request.httpMethod(), uri.getAuthority(), uri.getPath(),
                            response.status(), (System.nanoTime() - started) / 1_000_000);
                    return response;
                } catch (java.io.IOException | RuntimeException e) {
                    log.warn("event=DOWNSTREAM_FAILED service={} instance={} requestId={} method={} target={} path={} durationMs={} failure={}",
                            service, instance, requestId, request.httpMethod(), uri.getAuthority(), uri.getPath(),
                            (System.nanoTime() - started) / 1_000_000, e.getClass().getSimpleName());
                    throw e;
                }
            };
        }
    }
}
