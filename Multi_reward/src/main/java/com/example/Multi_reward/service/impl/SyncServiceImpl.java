package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.controller.cmd.PlayCmd;
import com.example.Multi_reward.entity.PlayRecord;
import com.example.Multi_reward.entity.RewardEvaluation;
import com.example.Multi_reward.entity.RewardRetryMessage;
import com.example.Multi_reward.exception.IllegalDurationTimeException;
import com.example.Multi_reward.integration.PrizeIntegration;
import com.example.Multi_reward.service.PlayRecordService;
import com.example.Multi_reward.service.SyncService;
import com.example.Multi_reward.service.UserAnalysisService;
import com.example.Multi_reward.service.producer.RetryProducer;
import com.example.Multi_reward.util.DateTimeUtil;
import com.example.Multi_reward.util.LogContext;
import com.example.Multi_reward.util.RewardCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static com.example.Multi_reward.constant.Constants.PRIZE_AMOUNT_RULE_CODE;
import static com.example.Multi_reward.constant.Constants.PRIZE_STAGE_RULE_CODE;

@Service
public class SyncServiceImpl implements SyncService {

    @Autowired private PlayRecordService playRecordService;
    @Autowired private UserAnalysisService userAnalysisService;
    @Autowired private RewardCalculator rewardCalculator;
    @Autowired private PrizeIntegration prizeIntegration;
    @Autowired private RetryProducer retryProducer;

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    @Override
    public void sync(PlayCmd playCmd) {
        LogContext.setUserId(playCmd.getUserId());
        checkDuration(playCmd);
        addPlayRecord(playCmd);

        String today = DateTimeUtil.format(
                DateTimeUtil.parse(playCmd.getSyncTime(), "yyyy-MM-dd HH:mm:ss"),
                "yyyy-MM-dd");

        int totalDuration = getTotalDuration(playCmd);
        userAnalysisService.upsert(playCmd.getUserId(), today, totalDuration);

        RewardEvaluation evaluation = calculateReward(totalDuration);
        if (!evaluation.hasReward()) {
            return;
        }

        log.info("event=REWARD_EVALUATED userId={} stage={} amount={}",
                playCmd.getUserId(), evaluation.getStage(), evaluation.getAmount());

        String outbizno = buildOutBizNo(playCmd, today, evaluation.getStage());
        RewardRetryMessage retryMessage = new RewardRetryMessage(
                evaluation.getStage(), evaluation.getAmount(), today, outbizno, playCmd);

        try {
            sendPrize(retryMessage);
        } catch (DuplicateKeyException e) {
            log.info("event=PRIZE_DUPLICATE_SKIPPED userId={} stage={} outbizno={}",
                    playCmd.getUserId(), evaluation.getStage(), outbizno);
        } catch (Exception e) {
            log.warn("event=PRIZE_SEND_FAILED userId={} outbizno={} error={}, enqueuing retry",
                    playCmd.getUserId(), outbizno, e.getMessage());
            retryProducer.send(retryMessage);
        }
    }

    @Transactional
    @Override
    public void sendPrize(RewardRetryMessage retryMessage) {
        // 奖品 code 是否存在由 PrizeCenter 校验(单一事实来源),不存在时透传具体错误进重试日志
        prizeIntegration.sendPrize(
                retryMessage.getPlayCmd(),
                retryMessage.getStage(),
                retryMessage.getAmount(),
                retryMessage.getToday(),
                retryMessage.getOutbizno());
    }

    private void checkDuration(PlayCmd playCmd) {
        if (playCmd.getDuration() > 30) {
            log.error("event=DURATION_INVALID userId={} duration={}",
                    playCmd.getUserId(), playCmd.getDuration());
            throw new IllegalDurationTimeException("illegal duration");
        }
    }

    private void addPlayRecord(PlayCmd playCmd) {
        Date syncTime = DateTimeUtil.parse(playCmd.getSyncTime(), "yyyy-MM-dd HH:mm:ss");
        PlayRecord record = new PlayRecord();
        record.setUserId(playCmd.getUserId());
        record.setMusicId(playCmd.getMusicId());
        record.setDuration(playCmd.getDuration());
        record.setSyncTime(syncTime);
        playRecordService.addRecord(record);
    }

    private int getTotalDuration(PlayCmd playCmd) {
        List<PlayRecord> records = playRecordService.getRecordsByDate(
                DateTimeUtil.getCurrentDateStart(), DateTimeUtil.getCurrentDateEnd(), playCmd.getUserId());
        return records.stream().mapToInt(PlayRecord::getDuration).sum();
    }

    private RewardEvaluation calculateReward(int totalDuration) {
        int stage = rewardCalculator.evaluateStage(PRIZE_STAGE_RULE_CODE, totalDuration);
        int amount = rewardCalculator.evaluateAmount(PRIZE_AMOUNT_RULE_CODE, totalDuration);
        return new RewardEvaluation(stage, amount);
    }

    private String buildOutBizNo(PlayCmd playCmd, String today, int stage) {
        return playCmd.getScene() + "_" + playCmd.getUserId() + "_"
                + playCmd.getCode() + "_" + today + "_" + stage;
    }
}