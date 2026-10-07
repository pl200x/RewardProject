package com.example.ai_chatbot.integration;

import com.example.ai_chatbot.integration.client.RewardClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIChatIntegrationTest extends FeignTestSupport {
    private AIChatIntegration integration() {
        return new AIChatIntegration(context.getBean(RewardClient.class));
    }

    @Test
    void retrievesAllRewardConfigurations() {
        responseBody = """
                {"configurationVoList":[{"id":1,"code":"STAGE_RULE",
                "rule":"stage = totalDuration < 160 ? 0 : 1","description":"Listening reward stage rule"}],
                "baseVO":{"success":true,"code":200,"duration":2,"errorMessage":null}}
                """;
        var result = integration().getAllConfigurations();
        assertEquals(1, result.size());
        assertEquals("STAGE_RULE", result.get(0).getCode());
        assertEquals("stage = totalDuration < 160 ? 0 : 1", result.get(0).getRule());
        assertEquals("GET", requests.get(0).method());
        assertEquals("/config/searchall", requests.get(0).uri().getPath());
    }

    @Test
    void retrievesPrizeRecordsByBusinessDate() {
        responseBody = """
                {"prizeRecordVOList":[{"id":1,"userId":3,"prizeDate":"2026-08-11","prizeStage":2,"prizeAmount":1}],
                "baseVO":{"success":true,"code":200,"duration":2,"errorMessage":null}}
                """;
        assertEquals(1, integration().getByUserAndPrizeDate(3, "2026-08-11").size());
        assertEquals("/prizerecord/by-user-and-date", requests.get(0).uri().getPath());
        assertEquals("userId=3&prizeDate=2026-08-11", requests.get(0).uri().getQuery());
    }

    @Test
    void retrievesDateRangeWithEncodedParameters() {
        responseBody = "{\"prizeRecordVOList\":[],\"baseVO\":{\"success\":true,\"code\":200,\"duration\":0}}";
        assertEquals(0, integration().getByUserAndDateRange("2026-08-11 00:00:00", "2026-08-11 23:59:59", 3).size());
        assertEquals("/prizerecord/by-user-and-date-range", requests.get(0).uri().getPath());
        assertEquals("startTime=2026-08-11 00:00:00&endTime=2026-08-11 23:59:59&userId=3",
                requests.get(0).uri().getQuery());
    }

    @Test
    void retrievesPlaybackWithAuthenticatedUserParameter() {
        responseBody = "{\"playRecordVOList\":[],\"baseVO\":{\"success\":true,\"code\":200,\"duration\":0}}";
        assertEquals(0, integration().findByDate("2026-08-11 00:00:00", "2026-08-11 23:59:59", 3).size());
        assertEquals("/playrecord/today", requests.get(0).uri().getPath());
        assertEquals("startTime=2026-08-11 00:00:00&endTime=2026-08-11 23:59:59&userId=3",
                requests.get(0).uri().getQuery());
    }

    @Test
    void rejectsBusinessFailureWithoutReturningInventedData() {
        responseBody = "{\"baseVO\":{\"success\":false,\"code\":500,\"duration\":0,\"errorMessage\":\"unavailable\"}}";
        assertTrue(assertThrows(IllegalStateException.class, () -> integration().getAllConfigurations())
                .getMessage().contains("unavailable"));
    }

    @Test
    void rejectsEmptyResponse() {
        responseBody = "";
        assertThrows(IllegalStateException.class, () -> integration().getAllConfigurations());
    }
}
