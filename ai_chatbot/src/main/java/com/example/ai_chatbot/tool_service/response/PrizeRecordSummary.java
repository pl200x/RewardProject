package com.example.ai_chatbot.tool_service.response;

import com.example.ai_chatbot.integration.VO.PrizeRecordVO;

import java.util.List;

public record PrizeRecordSummary(
        String startTime,
        String endTime,
        int recordCount,
        int totalAwardAmount,
        List<PrizeRecordVO> records) {
}
