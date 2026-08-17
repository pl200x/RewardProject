package com.example.ai_chatbot.tool_service.response;

public record StageRewardComparison(
        int stage,
        Integer reachedAtDurationSeconds,
        int expectedRecordCount,
        int actualRecordCount,
        int expectedAwardAmount,
        int actualAwardAmount,
        int amountDifference,
        boolean matched) {
}
