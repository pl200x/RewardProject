package com.example.ai_chatbot.tool_service;

import com.example.ai_chatbot.integration.AIChatIntegration;
import com.example.ai_chatbot.integration.VO.ConfigurationVO;
import com.example.ai_chatbot.integration.VO.PlayRecordVO;
import com.example.ai_chatbot.integration.VO.PrizeRecordVO;
import com.example.ai_chatbot.tool_service.response.DailyRewardComparison;
import com.example.ai_chatbot.tool_service.response.ExpectedRewardSummary;
import com.example.ai_chatbot.tool_service.response.PlayRecordSummary;
import com.example.ai_chatbot.tool_service.response.PrizeRecordSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolServiceTest {

    private StubAIChatIntegration integration;
    private ToolService toolService;
    private ToolContext context;

    @BeforeEach
    void setUp() {
        integration = new StubAIChatIntegration();
        toolService = new ToolService(integration);
        context = new ToolContext(Map.of("userId", 3));
    }

    @Test
    void calculatesPlaybackDurationDeterministically() {
        PlayRecordVO first = new PlayRecordVO(
                1, null, null, 3, 101, 30, "2026-08-11 08:00:00");
        PlayRecordVO second = new PlayRecordVO(
                2, null, null, 3, 102, 25, "2026-08-11 08:01:00");
        integration.playRecords = List.of(first, second);

        PlayRecordSummary result = toolService.findByDate(
                "2026-08-11 00:00:00", "2026-08-11 09:00:00", context);

        assertEquals(2, result.recordCount());
        assertEquals(55, result.totalDurationSeconds());
    }

    @Test
    void calculatesTotalAwardAmountDeterministically() {
        PrizeRecordVO first = prizeRecord(1, 1);
        PrizeRecordVO second = prizeRecord(2, 2);
        integration.prizeRecords = List.of(first, second);

        PrizeRecordSummary result = toolService.getTotalPrize(
                "2026-08-11 00:00:00", "2026-08-11 09:00:00", context);

        assertEquals(2, result.recordCount());
        assertEquals(3, result.totalAwardAmount());
    }

    @Test
    void rejectsAnInvertedDateRange() {
        assertThrows(IllegalArgumentException.class, () -> toolService.findByDate(
                "2026-08-11 09:00:00", "2026-08-11 00:00:00", context));
    }

    @Test
    void validatesInputsForTheSplitRewardTools() {
        assertThrows(IllegalArgumentException.class,
                () -> toolService.compareRewardsForDate("2026-08-11 09:00:00", 100, context));
        assertThrows(IllegalArgumentException.class,
                () -> toolService.calculateExpectedRewardsForDuration(-1));
    }

    @Test
    void calculatesExpectedRewardsForAKnownDailyDuration() {
        integration.configurations = rewardConfigurations();

        ExpectedRewardSummary result =
                toolService.calculateExpectedRewardsForDuration(700);

        assertEquals(700, result.totalDurationSeconds());
        assertEquals(3, result.highestStage());
        assertEquals(3, result.expectedRecordCount());
        assertEquals(4, result.expectedAwardAmount());
        assertEquals(160, result.stageRewards().get(0).reachedAtDurationSeconds());
        assertEquals(2, result.stageRewards().get(2).expectedAwardAmount());
        assertEquals(0, integration.findByDateCalls);
        assertEquals(0, integration.prizeByDateCalls);
        assertEquals(1, integration.configurationCalls);
    }

    @Test
    void comparesEveryReachedStageAndReportsAMatch() {
        integration.configurations = rewardConfigurations();
        integration.prizeRecords = List.of(
                prizeRecord(1, 1),
                prizeRecord(2, 1),
                prizeRecord(3, 2));

        DailyRewardComparison result =
                toolService.compareRewardsForDate("2026-08-11", 700, context);

        assertEquals(700, result.totalDurationSeconds());
        assertEquals(3, result.expectedRecordCount());
        assertEquals(3, result.actualRecordCount());
        assertEquals(4, result.expectedAwardAmount());
        assertEquals(4, result.actualAwardAmount());
        assertEquals(true, result.matched());
        assertEquals(false, result.hasProblem());
        assertEquals("MATCHED", result.status());
        assertEquals(3, result.stageComparisons().size());
        assertEquals(0, integration.findByDateCalls);
        assertEquals(1, integration.prizeByDateCalls);
        assertEquals(1, integration.configurationCalls);
    }

    @Test
    void reportsMissingStageRewardsAsUnderIssued() {
        integration.configurations = rewardConfigurations();
        integration.prizeRecords = List.of(prizeRecord(1, 1));

        DailyRewardComparison result =
                toolService.compareRewardsForDate("2026-08-11", 700, context);

        assertEquals(4, result.expectedAwardAmount());
        assertEquals(1, result.actualAwardAmount());
        assertEquals(-3, result.amountDifference());
        assertEquals(false, result.matched());
        assertEquals(true, result.hasProblem());
        assertEquals("UNDER_ISSUED", result.status());
    }

    @Test
    void treatsWrongStageRecordsAsAProblemEvenWhenGrandTotalsMatch() {
        integration.configurations = rewardConfigurations();
        integration.prizeRecords = List.of(prizeRecord(1, 4));

        DailyRewardComparison result =
                toolService.compareRewardsForDate("2026-08-11", 700, context);

        assertEquals(4, result.expectedAwardAmount());
        assertEquals(4, result.actualAwardAmount());
        assertEquals(false, result.matched());
        assertEquals("STAGE_OR_RECORD_MISMATCH", result.status());
    }

    @Test
    void evaluatesMultiDayRewardsIndependently() {
        integration.configurations = rewardConfigurations();
        integration.prizeRecords = List.of(
                prizeRecord(1, 1, "2026-08-10"),
                prizeRecord(2, 1, "2026-08-10"),
                prizeRecord(1, 1, "2026-08-11"));

        DailyRewardComparison firstDay =
                toolService.compareRewardsForDate("2026-08-10", 400, context);
        DailyRewardComparison secondDay =
                toolService.compareRewardsForDate("2026-08-11", 300, context);

        assertEquals(2, firstDay.expectedAwardAmount());
        assertEquals(2, firstDay.actualAwardAmount());
        assertEquals(true, firstDay.matched());
        assertEquals(1, secondDay.expectedAwardAmount());
        assertEquals(1, secondDay.actualAwardAmount());
        assertEquals(true, secondDay.matched());
    }

    @Test
    void publishesTheSmallIntentSpecificCustomerSupportTools() {
        ToolCallback[] callbacks = ToolCallbacks.from(toolService);
        Set<String> names = Arrays.stream(callbacks)
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "findByDate",
                "getTotalPrize",
                "getRewardConfigurations",
                "calculateExpectedRewardsForDuration",
                "compareRewardsForDate"), names);
    }

    private PrizeRecordVO prizeRecord(int stage, int amount) {
        return prizeRecord(stage, amount, "2026-08-11");
    }

    private PrizeRecordVO prizeRecord(int stage, int amount, String prizeDate) {
        PrizeRecordVO record = new PrizeRecordVO();
        record.setUserId(3);
        record.setPrizeDate(prizeDate);
        record.setPrizeStage(stage);
        record.setPrizeAmount(amount);
        return record;
    }

    private List<ConfigurationVO> rewardConfigurations() {
        ConfigurationVO stage = new ConfigurationVO();
        stage.setCode("STAGE_RULE");
        stage.setRule("stage = totalDuration < 160 ? 0 : totalDuration < 320 ? 1 : "
                + "totalDuration < 640 ? 2 : 3");
        ConfigurationVO amount = new ConfigurationVO();
        amount.setCode("AMOUNT_RULE");
        amount.setRule("amount = totalDuration < 160 ? 0 : totalDuration < 640 ? 1 : 2");
        return List.of(stage, amount);
    }

    private static class StubAIChatIntegration extends AIChatIntegration {
        private List<PlayRecordVO> playRecords = List.of();
        private List<PrizeRecordVO> prizeRecords = List.of();
        private List<ConfigurationVO> configurations = List.of();
        private int findByDateCalls;
        private int prizeByDateCalls;
        private int configurationCalls;

        private StubAIChatIntegration() {
            super(null);
        }

        @Override
        public List<PlayRecordVO> findByDate(String startTime, String endTime, int userId) {
            findByDateCalls++;
            return playRecords;
        }

        @Override
        public List<PrizeRecordVO> getByUserAndDateRange(
                String startTime, String endTime, int userId) {
            return prizeRecords;
        }

        @Override
        public List<PrizeRecordVO> getByUserAndPrizeDate(int userId, String prizeDate) {
            prizeByDateCalls++;
            return prizeRecords.stream()
                    .filter(record -> prizeDate.equals(record.getPrizeDate()))
                    .toList();
        }

        @Override
        public List<ConfigurationVO> getAllConfigurations() {
            configurationCalls++;
            return configurations;
        }
    }
}
