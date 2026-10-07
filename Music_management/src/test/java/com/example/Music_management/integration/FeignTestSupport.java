package com.example.Music_management.integration;

import com.example.Music_management.config.FeignClientConfig;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** Uses actual Spring-managed Feign proxies and codecs against a local HTTP fixture. */
abstract class FeignTestSupport {
    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration(excludeName = {"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "org.redisson.spring.starter.RedissonAutoConfigurationV2"})
    @Import(FeignClientConfig.class)
    static class ClientApplication { }

    protected ConfigurableApplicationContext context;
    protected final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();
    protected volatile String responseBody = "{\"success\":true,\"code\":200}";
    protected volatile int responseStatus = 200;
    protected volatile long delayMillis;
    private HttpServer server;
    private ExecutorService executor;

    record RecordedRequest(String method, URI uri, Map<String, List<String>> headers, String body) {
        String header(String name) {
            return headers.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name))
                    .flatMap(e -> e.getValue().stream()).findFirst().orElse(null);
        }
    }

    @BeforeEach
    void startClient() throws Exception {
        executor = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            requests.add(new RecordedRequest(exchange.getRequestMethod(), exchange.getRequestURI(),
                    Map.copyOf(exchange.getRequestHeaders()),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            try {
                if (delayMillis > 0) Thread.sleep(delayMillis);
                byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                if (responseStatus == 503) exchange.getResponseHeaders().set("Retry-After", "0");
                exchange.sendResponseHeaders(responseStatus, bytes.length == 0 ? -1 : bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { exchange.close(); }
        });
        server.start();
        context = new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.NONE).registerShutdownHook(false).logStartupInfo(false).run(
                        "--spring.profiles.active=direct",
                        "--eureka.client.enabled=false",
                        "--spring.ai.openai.api-key=test-key",
                        "--integration.multireward.base-url=http://127.0.0.1:" + server.getAddress().getPort(),
                        "--integration.http.connect-timeout-ms=200",
                        "--integration.http.read-timeout-ms=150");
    }

    @AfterEach
    void stopClient() {
        if (context != null) context.close();
        if (server != null) server.stop(0);
        if (executor != null) executor.shutdownNow();
    }
}
