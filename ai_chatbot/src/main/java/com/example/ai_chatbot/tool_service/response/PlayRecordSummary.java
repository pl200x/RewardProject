package com.example.ai_chatbot.tool_service.response;

import com.example.ai_chatbot.integration.VO.PlayRecordVO;

import java.util.List;

public record PlayRecordSummary(
        String startTime,
        String endTime,
        int recordCount,
        int totalDurationSeconds,
        List<PlayRecordVO> records) {
}
