package com.example.serviceregistry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceRegistryApplicationTests {
    @Autowired
    private Environment environment;

    @Test
    void registryAcceptsRegistrationAndDeregistration() throws Exception {
        String base = "http://localhost:" + environment.getProperty("local.server.port");
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        String instance = """
                {"instance":{"instanceId":"registry-test","app":"PRIZE-SERVICE",
                "hostName":"localhost","ipAddr":"127.0.0.1","status":"UP",
                "port":{"$":18082,"@enabled":"true"},"vipAddress":"prize-service",
                "dataCenterInfo":{"@class":"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo","name":"MyOwn"}}}
                """;
        HttpResponse<String> registration = client.send(HttpRequest.newBuilder(
                URI.create(base + "/eureka/apps/PRIZE-SERVICE"))
                .timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(instance)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(204, registration.statusCode());
        try {
            HttpResponse<String> lookup = client.send(HttpRequest.newBuilder(
                    URI.create(base + "/eureka/apps/PRIZE-SERVICE/registry-test"))
                    .timeout(Duration.ofSeconds(5)).header("Accept", "application/json").build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, lookup.statusCode());
            assertTrue(lookup.body().contains("registry-test"));
        } finally {
            HttpResponse<String> deletion = client.send(HttpRequest.newBuilder(
                    URI.create(base + "/eureka/apps/PRIZE-SERVICE/registry-test"))
                    .timeout(Duration.ofSeconds(5)).DELETE().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, deletion.statusCode());
        }
    }
}
