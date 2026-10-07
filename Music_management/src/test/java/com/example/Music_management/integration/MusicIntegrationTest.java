package com.example.Music_management.integration;

import com.example.Music_management.integration.client.RewardClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class MusicIntegrationTest extends FeignTestSupport {
    private MusicIntegration integration() {
        return new MusicIntegration(context.getBean(RewardClient.class));
    }

    @Test
    void preservesPlaybackBodyAndFailureEnvelope() throws Exception {
        PlayRecordCmd command = new PlayRecordCmd(7, 1, 30, "2026-10-04 12:00:00", "Listening_Music", "coin");
        integration().syncMusicPlayRecord(command);
        var request = requests.get(0);
        assertEquals("POST", request.method());
        assertEquals("/sync", request.uri().getPath());
        var json = new ObjectMapper().readTree(request.body());
        assertEquals(7, json.get("userId").asInt());
        assertEquals(30, json.get("duration").asInt());
        assertEquals("2026-10-04 12:00:00", json.get("syncTime").asText());
        responseBody = "{\"success\":false}";
        assertThrows(RuntimeException.class, () -> integration().syncMusicPlayRecord(command));
        assertEquals(2, requests.size());
    }

    @Test
    void directRollbackHasBoundedPostTimeoutAndDoesNotRetry() {
        delayMillis = 700;
        long started = System.nanoTime();
        assertThrows(RetryableException.class, () -> integration().syncMusicPlayRecord(new PlayRecordCmd()));
        assertTrue((System.nanoTime() - started) / 1_000_000 < 2000);
        assertEquals(1, requests.size());
    }

    @Test
    void propagatesOnlyRequestCorrelationFromMdc() {
        try {
            MDC.put("traceId", "request-123");
            integration().syncMusicPlayRecord(new PlayRecordCmd());
            assertEquals("request-123", requests.get(0).header("X-Request-ID"));
            assertNull(requests.get(0).header("Authorization"));
            assertEquals("request-123", MDC.get("traceId"));
        } finally { MDC.clear(); }
    }

    @Test
    void generatesCorrelationForBackgroundCallsWithoutLeakingMdc() {
        MDC.clear();
        integration().syncMusicPlayRecord(new PlayRecordCmd());
        assertNotNull(requests.get(0).header("X-Request-ID"));
        assertNull(MDC.get("traceId"));
    }
}
