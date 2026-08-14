package com.example.ai_chatbot.tool_service.response;

import java.util.List;

public record ExpectedRewardSummary(
        int totalDurationSeconds,
        String stageRule,
        String amountRule,
        int highestStage,
        int expectedRecordCount,
        int expectedAwardAmount,
        List<ExpectedStageReward> stageRewards) {
}
