package com.example.ai_chatbot.tool_service.response;

public record ExpectedStageReward(
        int stage,
        int reachedAtDurationSeconds,
        int expectedAwardAmount) {
}
