package com.example.Multi_reward.integration;

import com.example.Multi_reward.config.FeignClientConfig;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import feign.RetryableException;
import feign.FeignException;

import static org.junit.jupiter.api.Assertions.*;

/** Real Eureka + real application integration/client; only downstream business data is a fixture. */
class DiscoveryLoadBalancerIT {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
    private Process registry;
    private int registryPort;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class,
            RedisRepositoriesAutoConfiguration.class, KafkaAutoConfiguration.class})
    @Import({FeignClientConfig.class, CheckScheduleIntegration.class})
    static class ClientApplication { }

    @Test
    void discoveryBalancesConvergesAndRecoversWithoutRetryingTimedOutCalls() throws Exception {
        try (ServerSocket port = new ServerSocket(0)) { registryPort = port.getLocalPort(); }
        List<String> selections = new CopyOnWriteArrayList<>();
        try (Backend first = new Backend("pilot-a", selections); Backend second = new Backend("pilot-b", selections)) {
            startRegistry();
            register(first);
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ClientApplication.class)
                    .web(WebApplicationType.NONE).run(
                            "--eureka.client.service-url.defaultZone=" + registryUrl() + "/eureka/",
                            "--seata.enabled=false",
                        "--eureka.client.enabled=true",
                            "--spring.profiles.active=discovery-test",
                            "--eureka.client.register-with-eureka=false",
                            "--eureka.client.healthcheck.enabled=false",
                            "--eureka.client.registry-fetch-interval-seconds=1",
                            "--eureka.client.disable-delta=true",
                            "--spring.cloud.loadbalancer.cache.enabled=false",
                            "--spring.cloud.loadbalancer.retry.enabled=false",
                            "--integration.http.connect-timeout-ms=200",
                            "--integration.http.read-timeout-ms=150")) {
                DiscoveryClient discovery = context.getBean(DiscoveryClient.class);
                CheckScheduleIntegration integration = context.getBean(CheckScheduleIntegration.class);
                await(() -> discovery.getInstances("prize-service").size() == 1, "one instance discovered");
                assertEquals(7, integration.getPrizeCount());

                register(second);
                await(() -> discovery.getInstances("prize-service").size() == 2, "two instances discovered");
                int firstBefore = first.requests.get();
                selections.clear();
                for (int i = 0; i < 10; i++) assertEquals(7, integration.getPrizeCount());
                assertEquals(5, first.requests.get() - firstBefore);
                assertEquals(5, second.requests.get());
                assertEquals(10, selections.size());
                for (int i = 1; i < selections.size(); i++) {
                    assertNotEquals(selections.get(i - 1), selections.get(i), "round robin alternates instances");
                }

                registryRequest("PUT", "/eureka/apps/PRIZE-SERVICE/pilot-b/status?value=DOWN", "", 200);
                await(() -> discovery.getInstances("prize-service").size() == 1, "DOWN instance excluded");
                int secondBefore = second.requests.get();
                for (int i = 0; i < 4; i++) assertEquals(7, integration.getPrizeCount());
                assertEquals(secondBefore, second.requests.get());

                // An unavailable registry does not erase already fetched, still reachable instances.
                stopRegistry();
                assertEquals(7, integration.getPrizeCount());
                startRegistry();
                register(first);
                register(second);
                await(() -> discovery.getInstances("prize-service").size() == 2, "registry recovery");
                registryRequest("DELETE", "/eureka/apps/PRIZE-SERVICE/pilot-b", "", 200);
                second.close();
                await(() -> discovery.getInstances("prize-service").size() == 1, "shutdown convergence");
                assertEquals(7, integration.getPrizeCount());

                first.success = false;
                assertThrows(RuntimeException.class, integration::getPrizeCount);
                first.success = true;
                first.delayMillis = 700;
                int requestsBefore = first.requests.get();
                long started = System.nanoTime();
                assertThrows(RetryableException.class, integration::getPrizeCount);
                assertTrue(Duration.ofNanos(System.nanoTime() - started).toMillis() < 2000);
                assertEquals(requestsBefore + 1, first.requests.get(), "timeout must not retry");

                registryRequest("DELETE", "/eureka/apps/PRIZE-SERVICE/pilot-a", "", 200);
                await(() -> discovery.getInstances("prize-service").isEmpty(), "empty registry convergence");
                assertThrows(FeignException.ServiceUnavailable.class, integration::getPrizeCount);
            }
        } finally {
            stopRegistry();
        }
    }

    private String registryUrl() { return "http://127.0.0.1:" + registryPort; }

    private void startRegistry() throws Exception {
        Path jar = Path.of("../service-registry/target/service-registry-0.0.1-SNAPSHOT.jar").toAbsolutePath();
        assertTrue(jar.toFile().isFile(), "Build service-registry before running -Pdiscovery-it");
        registry = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xmx192m", "-jar", jar.toString(), "--server.port=" + registryPort,
                "--eureka.server.use-read-only-response-cache=false",
                "--eureka.server.enable-self-preservation=false")
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.appendTo(
                        Path.of("target/discovery-registry.log").toFile())).start();
        await(() -> {
            try {
                return HTTP.send(HttpRequest.newBuilder(URI.create(registryUrl() + "/actuator/health"))
                        .timeout(Duration.ofSeconds(1)).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
            } catch (Exception e) { return false; }
        }, "registry startup");
    }

    private void stopRegistry() throws InterruptedException {
        if (registry != null && registry.isAlive()) {
            registry.destroy();
            if (!registry.waitFor(10, TimeUnit.SECONDS)) {
                registry.destroyForcibly();
                registry.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private void register(Backend backend) throws Exception {
        registryRequest("POST", "/eureka/apps/PRIZE-SERVICE", """
                {"instance":{"instanceId":"%s","app":"PRIZE-SERVICE",
                "hostName":"127.0.0.1","ipAddr":"127.0.0.1","status":"UP",
                "port":{"$":%d,"@enabled":"true"},"vipAddress":"prize-service",
                "leaseInfo":{"durationInSecs":120},
                "dataCenterInfo":{"@class":"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo","name":"MyOwn"}}}
                """.formatted(backend.id, backend.server.getAddress().getPort()), 204);
    }

    private void registryRequest(String method, String path, String body, int expected) throws Exception {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(registryUrl() + path))
                .timeout(Duration.ofSeconds(3)).header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(expected, response.statusCode(), response.body());
    }

    private static void await(BooleanSupplier condition, String description) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(40).toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return;
            Thread.sleep(200);
        }
        fail("Timed out waiting for " + description);
    }

    private static class Backend implements AutoCloseable {
        final String id;
        final HttpServer server;
        final ExecutorService executor = Executors.newCachedThreadPool();
        final AtomicInteger requests = new AtomicInteger();
        volatile boolean success = true;
        volatile long delayMillis;

        Backend(String id, List<String> selections) throws IOException {
            this.id = id;
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/prize_reward/count", exchange -> {
                requests.incrementAndGet();
                selections.add(id);
                try {
                    if (delayMillis > 0) Thread.sleep(delayMillis);
                    byte[] body = ("{\"count\":7,\"baseVO\":{\"success\":" + success
                            + ",\"code\":200,\"errorMessage\":null}}").getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.getResponseHeaders().add("X-Service-Instance", id);
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    exchange.close();
                }
            });
            server.start();
        }

        public void close() {
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
