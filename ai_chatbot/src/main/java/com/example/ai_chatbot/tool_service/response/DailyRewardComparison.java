package com.example.ai_chatbot.tool_service.response;

import java.util.List;

public record DailyRewardComparison(
        String date,
        String stageRule,
        String amountRule,
        int totalDurationSeconds,
        int expectedHighestStage,
        int expectedRecordCount,
        int actualRecordCount,
        int expectedAwardAmount,
        int actualAwardAmount,
        int amountDifference,
        boolean matched,
        boolean hasProblem,
        String status,
        List<StageRewardComparison> stageComparisons) {
}
