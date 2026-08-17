package com.example.ai_chatbot.tool_service;

import com.example.ai_chatbot.integration.AIChatIntegration;
import com.example.ai_chatbot.integration.VO.ConfigurationVO;
import com.example.ai_chatbot.integration.VO.PlayRecordVO;
import com.example.ai_chatbot.integration.VO.PrizeRecordVO;
import com.example.ai_chatbot.tool_service.response.DailyRewardComparison;
import com.example.ai_chatbot.tool_service.response.ExpectedRewardSummary;
import com.example.ai_chatbot.tool_service.response.ExpectedStageReward;
import com.example.ai_chatbot.tool_service.response.PlayRecordSummary;
import com.example.ai_chatbot.tool_service.response.PrizeRecordSummary;
import com.example.ai_chatbot.tool_service.response.StageRewardComparison;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ToolService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
    private static final String STAGE_RULE_CODE = "STAGE_RULE";
    private static final String AMOUNT_RULE_CODE = "AMOUNT_RULE";
    private static final JexlEngine RULE_ENGINE = new JexlBuilder()
            .strict(true)
            .silent(false)
            .create();

    private final AIChatIntegration chatIntegration;

    public ToolService(AIChatIntegration chatIntegration) {
        this.chatIntegration = chatIntegration;
    }

    @Tool(description = """
            Retrieve prize records already issued to the current user in an inclusive date-time range.
            Use this tool when the user asks what prizes they received, how many reward units they received,
            or asks to see raw prize history. The response includes the records, record count, and a deterministic
            totalAwardAmount calculated by the application. For a reward-consistency question, first retrieve the
            listening duration for one calendar day with findByDate, then call compareRewardsForDate.
            """)
    public PrizeRecordSummary getTotalPrize(
            @ToolParam(description = """
                    Inclusive start of the requested range in yyyy-MM-dd HH:mm:ss format.
                    Resolve relative dates from the current local date-time in the system message.
                    For "past N days", include today and use 00:00:00 on the date N-1 calendar days ago.
                    """, required = true)
            String startTime,
            @ToolParam(description = """
                    Inclusive end of the requested range in yyyy-MM-dd HH:mm:ss format.
                    For "past N days" or "today", use the current local date-time from the system message.
                    """, required = true)
            String endTime,
            ToolContext toolContext) {
        validateRange(startTime, endTime);
        int userId = currentUserId(toolContext);
        List<PrizeRecordVO> records = safeList(
                chatIntegration.getByUserAndDateRange(startTime, endTime, userId));
        int totalAwardAmount = records.stream().mapToInt(PrizeRecordVO::getPrizeAmount).sum();
        return new PrizeRecordSummary(
                startTime, endTime, records.size(), totalAwardAmount, records);
    }

    @Tool(description = """
            Retrieve playback records for the current user in an inclusive date-time range.
            Use this tool for listening duration, play count, raw listening history, or as the first step of a
            reward-consistency check.
            The response includes the records and totalDurationSeconds, which is calculated deterministically
            by the application. Always report totalDurationSeconds; do not manually re-sum record durations.
            Treat every new explicit time request independently and never reuse dates from chat history.
            """)
    public PlayRecordSummary findByDate(
            @ToolParam(description = """
                    Inclusive start of the requested range in yyyy-MM-dd HH:mm:ss format.
                    Resolve relative dates from the current local date-time in the system message.
                    For "past N days", include today and use 00:00:00 on the date N-1 calendar days ago.
                    Example rule only: past 3 days starts at 00:00:00 two calendar dates before today.
                    """, required = true)
            String startTime,
            @ToolParam(description = """
                    Inclusive end of the requested range in yyyy-MM-dd HH:mm:ss format.
                    For "past N days" or "today", use the current local date-time from the system message.
                    """, required = true)
            String endTime,
            ToolContext toolContext) {
        validateRange(startTime, endTime);
        int userId = currentUserId(toolContext);
        List<PlayRecordVO> records = safeList(
                chatIntegration.findByDate(startTime, endTime, userId));
        int totalDurationSeconds = records.stream().mapToInt(PlayRecordVO::getDuration).sum();
        return new PlayRecordSummary(
                startTime, endTime, records.size(), totalDurationSeconds, records);
    }

    @Tool(description = """
            Retrieve all currently active listening-reward configurations.
            Use this tool only when the user asks how to earn prizes or asks for the current reward rules.
            STAGE_RULE determines the reward stage from a day's cumulative totalDuration in seconds; AMOUNT_RULE
            determines the award amount for the resulting stage. This tool is read-only and never issues a prize.
            """)
    public List<ConfigurationVO> getRewardConfigurations() {
        return safeList(chatIntegration.getAllConfigurations());
    }

    @Tool(description = """
            Calculate the expected listening rewards for one local calendar day's cumulative playback duration.
            Use this tool when the user asks what they should earn for a known duration. If the duration is not
            stated by the user, call findByDate for exactly one calendar day first and pass its exact
            totalDurationSeconds here. This tool reads the current reward configurations, but does not retrieve or
            compare issued prize records. Never pass a duration combined from multiple calendar days.
            """)
    public ExpectedRewardSummary calculateExpectedRewardsForDuration(
            @ToolParam(description = """
                    One local calendar day's cumulative playback duration in seconds. Use the exact
                    totalDurationSeconds returned by findByDate; do not estimate or convert it in the model.
                    """, required = true)
            int totalDurationSeconds) {
        validateDuration(totalDurationSeconds);
        RewardRules rules = loadRewardRules();
        ExpectedRewardCalculation expected = calculateExpectedRewards(
                totalDurationSeconds, rules.stageExpression(), rules.amountExpression());

        return new ExpectedRewardSummary(
                totalDurationSeconds,
                rules.stageRule(),
                rules.amountRule(),
                expected.highestStage(),
                expected.stageRewards().size(),
                expected.stageRewards().stream()
                        .mapToInt(ExpectedStageReward::expectedAwardAmount).sum(),
                expected.stageRewards());
    }

    @Tool(description = """
            Deterministically compare rewards issued on one local calendar date with the rewards expected for a
            verified playback duration. Use this for a missing, excessive, incorrect, or suspicious reward report.
            First call findByDate from 00:00:00 through the end of this same date, then pass its exact
            totalDurationSeconds and the date here. This tool retrieves that date's issued prize records and the
            current reward configurations. It compares every stage's record count and award amount and returns the
            final matched/status verdict. Do not call getTotalPrize or calculateExpectedRewardsForDuration as extra
            steps unless the user separately asks for raw records or expected rewards only.
            """)
    public DailyRewardComparison compareRewardsForDate(
            @ToolParam(description = """
                    Local calendar date to check in yyyy-MM-dd format. It must be the same date used for the
                    preceding one-day findByDate call.
                    """, required = true)
            String date,
            @ToolParam(description = """
                    Exact totalDurationSeconds returned by findByDate for this one local calendar date.
                    Never pass a multi-day total or a value calculated by the model.
                    """, required = true)
            int totalDurationSeconds,
            ToolContext toolContext) {
        LocalDate rewardDate = parseRewardDate(date);
        validateDuration(totalDurationSeconds);
        int userId = currentUserId(toolContext);
        List<PrizeRecordVO> prizeRecords = safeList(
                chatIntegration.getByUserAndPrizeDate(userId, rewardDate.toString()));
        RewardRules rules = loadRewardRules();

        return compareDay(rewardDate, totalDurationSeconds, prizeRecords, rules);
    }

    private DailyRewardComparison compareDay(
            LocalDate date,
            int totalDuration,
            List<PrizeRecordVO> actualRecords,
            RewardRules rules) {
        ExpectedRewardCalculation expected = calculateExpectedRewards(
                totalDuration, rules.stageExpression(), rules.amountExpression());
        Map<Integer, ExpectedStageReward> expectedByStage = expected.stageRewards().stream()
                .collect(Collectors.toMap(
                        ExpectedStageReward::stage,
                        reward -> reward,
                        (first, ignored) -> first,
                        LinkedHashMap::new));

        Map<Integer, List<PrizeRecordVO>> actualByStage = actualRecords.stream()
                .collect(Collectors.groupingBy(
                        PrizeRecordVO::getPrizeStage,
                        HashMap::new,
                        Collectors.toList()));
        Set<Integer> allStages = new LinkedHashSet<>(expectedByStage.keySet());
        actualByStage.keySet().stream().sorted().forEach(allStages::add);

        List<StageRewardComparison> stageComparisons = allStages.stream()
                .sorted(Comparator.naturalOrder())
                .map(stage -> compareStage(
                        stage, expectedByStage.get(stage), actualByStage.getOrDefault(stage, List.of())))
                .toList();

        int expectedAmount = stageComparisons.stream()
                .mapToInt(StageRewardComparison::expectedAwardAmount).sum();
        int actualAmount = stageComparisons.stream()
                .mapToInt(StageRewardComparison::actualAwardAmount).sum();
        int expectedCount = stageComparisons.stream()
                .mapToInt(StageRewardComparison::expectedRecordCount).sum();
        int actualCount = stageComparisons.stream()
                .mapToInt(StageRewardComparison::actualRecordCount).sum();
        boolean matched = stageComparisons.stream().allMatch(StageRewardComparison::matched);

        return new DailyRewardComparison(
                date.toString(),
                rules.stageRule(),
                rules.amountRule(),
                totalDuration,
                expected.highestStage(),
                expectedCount,
                actualCount,
                expectedAmount,
                actualAmount,
                actualAmount - expectedAmount,
                matched,
                !matched,
                comparisonStatus(matched, expectedAmount, actualAmount),
                stageComparisons);
    }

    private StageRewardComparison compareStage(
            int stage,
            ExpectedStageReward expected,
            List<PrizeRecordVO> actualRecords) {
        int expectedCount = expected == null ? 0 : 1;
        int expectedAmount = expected == null ? 0 : expected.expectedAwardAmount();
        int actualAmount = actualRecords.stream().mapToInt(PrizeRecordVO::getPrizeAmount).sum();
        boolean matched = actualRecords.size() == expectedCount && actualAmount == expectedAmount;

        return new StageRewardComparison(
                stage,
                expected == null ? null : expected.reachedAtDurationSeconds(),
                expectedCount,
                actualRecords.size(),
                expectedAmount,
                actualAmount,
                actualAmount - expectedAmount,
                matched);
    }

    private ExpectedRewardCalculation calculateExpectedRewards(
            int totalDuration,
            JexlExpression stageExpression,
            JexlExpression amountExpression) {
        int highestStage = evaluateRule(stageExpression, totalDuration);
        if (highestStage < 0) {
            throw new IllegalStateException("STAGE_RULE returned a negative stage");
        }

        List<ExpectedStageReward> stageRewards = new ArrayList<>();
        for (int stage = 1; stage <= highestStage; stage++) {
            int threshold = firstDurationForStage(stageExpression, stage, totalDuration);
            int stageAtThreshold = evaluateRule(stageExpression, threshold);
            if (stageAtThreshold != stage) {
                throw new IllegalStateException(
                        "STAGE_RULE skips stage " + stage + " and cannot be compared per stage");
            }
            int expectedAmount = evaluateRule(amountExpression, threshold);
            if (expectedAmount <= 0) {
                throw new IllegalStateException(
                        "AMOUNT_RULE returned a non-positive amount for reward stage " + stage);
            }
            stageRewards.add(new ExpectedStageReward(stage, threshold, expectedAmount));
        }
        return new ExpectedRewardCalculation(highestStage, List.copyOf(stageRewards));
    }

    private RewardRules loadRewardRules() {
        List<ConfigurationVO> configurations = safeList(
                chatIntegration.getAllConfigurations());
        String stageRule = requireRule(configurations, STAGE_RULE_CODE, "stage");
        String amountRule = requireRule(configurations, AMOUNT_RULE_CODE, "amount");
        return new RewardRules(
                stageRule,
                amountRule,
                compileRule(stageRule, "stage"),
                compileRule(amountRule, "amount"));
    }

    private int firstDurationForStage(
            JexlExpression stageExpression,
            int targetStage,
            int maximumDuration) {
        int low = 0;
        int high = maximumDuration;
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (evaluateRule(stageExpression, middle) >= targetStage) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }
        return low;
    }

    private String requireRule(
            List<ConfigurationVO> configurations,
            String requiredCode,
            String resultVariable) {
        return configurations.stream()
                .filter(configuration -> requiredCode.equalsIgnoreCase(configuration.getCode()))
                .map(ConfigurationVO::getRule)
                .filter(this::hasText)
                .findFirst()
                .or(() -> configurations.stream()
                        .map(ConfigurationVO::getRule)
                        .filter(rule -> startsWithAssignment(rule, resultVariable))
                        .findFirst())
                .or(() -> configurations.stream()
                        .map(ConfigurationVO::getCode)
                        .filter(rule -> startsWithAssignment(rule, resultVariable))
                        .findFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "Required reward configuration is missing: " + requiredCode));
    }

    private JexlExpression compileRule(String configuredRule, String resultVariable) {
        String expression = configuredRule.trim();
        int assignmentIndex = expression.indexOf('=');
        if (assignmentIndex >= 0) {
            String assignedVariable = expression.substring(0, assignmentIndex).trim();
            if (!resultVariable.equals(assignedVariable)) {
                throw new IllegalStateException(
                        "Unexpected assignment in " + resultVariable + " reward rule");
            }
            expression = expression.substring(assignmentIndex + 1).trim();
        }
        validateRuleCharacters(expression);
        return RULE_ENGINE.createExpression(expression);
    }

    private int evaluateRule(JexlExpression expression, int totalDuration) {
        JexlContext context = new MapContext();
        context.set("totalDuration", totalDuration);
        Object result = expression.evaluate(context);
        if (!(result instanceof Number number)) {
            throw new IllegalStateException("Reward rule did not return a numeric result");
        }
        return number.intValue();
    }

    private void validateRuleCharacters(String expression) {
        String operatorsAndNumbers = expression
                .replace("totalDuration", "")
                .replaceAll("\\d+(?:\\.\\d+)?", "")
                .replaceAll("[\\s()?:<>=!&|+*/%.\\-]", "");
        if (!operatorsAndNumbers.isEmpty()) {
            throw new IllegalStateException("Reward rule contains unsupported content");
        }
    }

    private boolean startsWithAssignment(String value, String variable) {
        return hasText(value) && value.trim().matches(variable + "\\s*=.*");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDate parseRewardDate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("date must use yyyy-MM-dd format");
        }
        try {
            LocalDate date = LocalDate.parse(value);
            if (!date.toString().equals(value)) {
                throw new DateTimeParseException("Date is not canonical", value, 0);
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("date must use yyyy-MM-dd format", e);
        }
    }

    private String comparisonStatus(boolean matched, int expectedAmount, int actualAmount) {
        if (matched) {
            return "MATCHED";
        }
        if (actualAmount < expectedAmount) {
            return "UNDER_ISSUED";
        }
        if (actualAmount > expectedAmount) {
            return "OVER_ISSUED";
        }
        return "STAGE_OR_RECORD_MISMATCH";
    }

    private int currentUserId(ToolContext toolContext) {
        Object userId = toolContext.getContext().get("userId");
        if (!(userId instanceof Number number)) {
            throw new IllegalArgumentException("userId is missing from the tool context");
        }
        return number.intValue();
    }

    private void validateRange(String startTime, String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("startTime must not be after endTime");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "startTime and endTime must use yyyy-MM-dd HH:mm:ss format", e);
        }
    }

    private void validateDuration(int totalDurationSeconds) {
        if (totalDurationSeconds < 0) {
            throw new IllegalArgumentException(
                    "totalDurationSeconds must not be negative");
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private record ExpectedRewardCalculation(
            int highestStage,
            List<ExpectedStageReward> stageRewards) {
    }

    private record RewardRules(
            String stageRule,
            String amountRule,
            JexlExpression stageExpression,
            JexlExpression amountExpression) {
    }
}
