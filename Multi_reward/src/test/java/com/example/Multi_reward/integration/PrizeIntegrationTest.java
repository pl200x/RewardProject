package com.example.Multi_reward.integration;

import com.example.Multi_reward.integration.client.PrizeClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrizeIntegrationTest extends FeignTestSupport {
    @Test
    void propagatesOnlyTheActiveXidAndDoesNotLeakItIntoTheNextCall() {
        var root = "127.0.0.1:18091:12345";
        org.apache.seata.core.context.RootContext.bind(root);
        try {
            integration().send("coin", 1, "with-xid");
            assertEquals(root, requests.get(0).header("TX_XID"));
        } finally {
            org.apache.seata.core.context.RootContext.unbind();
        }
        integration().send("coin", 1, "without-xid");
        assertNull(requests.get(1).header("TX_XID"));
    }

    private PrizeIntegration integration() {
        return new PrizeIntegration(context.getBean(PrizeClient.class));
    }

    @Test
    void sendsOriginalIdempotencyKeyAndPayloadOnce() throws Exception {
        integration().send("coin", 2, "original-key");
        assertEquals(1, requests.size());
        var request = requests.get(0);
        assertEquals("POST", request.method());
        assertEquals("/prize/send_reward", request.uri().getPath());
        assertTrue(request.header("Content-Type").startsWith("application/json"));
        assertEquals(new ObjectMapper().readTree("{\"code\":\"coin\",\"amount\":2,\"outbizno\":\"original-key\"}"),
                new ObjectMapper().readTree(request.body()));
    }

    @Test
    void preservesBusinessErrorForExistingKafkaRetryPath() {
        responseBody = "{\"success\":false,\"errorMessage\":\"inventory shortage\"}";
        assertTrue(assertThrows(RuntimeException.class, () -> integration().send("coin", 2, "key"))
                .getMessage().contains("inventory shortage"));
        assertEquals(1, requests.size());
    }

    @Test
    void doesNotRetryHttpFailureEvenWithRetryAfter() {
        responseStatus = 503;
        assertThrows(FeignException.class, () -> integration().send("coin", 2, "key"));
        assertEquals(1, requests.size());
    }

    @Test
    void doesNotRetryAnUncertainTimedOutAward() {
        delayMillis = 700;
        long started = System.nanoTime();
        assertThrows(RetryableException.class, () -> integration().send("coin", 2, "key"));
        assertTrue((System.nanoTime() - started) / 1_000_000 < 2000);
        assertEquals(1, requests.size());
    }
}
