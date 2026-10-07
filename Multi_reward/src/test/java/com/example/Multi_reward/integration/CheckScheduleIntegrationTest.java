package com.example.Multi_reward.integration;

import com.example.Multi_reward.integration.client.PrizeClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckScheduleIntegrationTest extends FeignTestSupport {
    @Test
    void preservesBothReconciliationResponseShapes() {
        CheckScheduleIntegration integration = new CheckScheduleIntegration(context.getBean(PrizeClient.class));
        responseBody = "{\"count\":3,\"baseVO\":{\"success\":true}}";
        assertEquals(3, integration.getPrizeCount());
        responseBody = "{\"prizeRecordList\":[],\"baseVO\":{\"success\":true}}";
        assertTrue(integration.getPrizeRecordList().isEmpty());
        assertEquals("GET", requests.get(0).method());
        assertEquals("/prize_reward/count", requests.get(0).uri().getPath());
        assertEquals("/prize_reward/record-by-minutes", requests.get(1).uri().getPath());
    }
}
