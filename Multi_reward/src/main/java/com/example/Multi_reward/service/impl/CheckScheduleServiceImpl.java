package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.integration.CheckScheduleIntegration;
import com.example.Multi_reward.integration.PrizeRecord;
import com.example.Multi_reward.service.CheckScheduleService;
import com.example.Multi_reward.service.PrizeRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CheckScheduleServiceImpl implements CheckScheduleService {

    @Autowired
    private PrizeRecordService prizeRecordService;
    @Autowired
    private CheckScheduleIntegration checkScheduleIntegration;

    private static final Logger log = LoggerFactory.getLogger(CheckScheduleServiceImpl.class);

    @Override
    public void checkAmount() {
        int multiRewardCount = prizeRecordService.getCount();
        int prizeCenterCount = checkScheduleIntegration.getPrizeCount();
        log.info("event=AMOUNT_CHECK_RESULT multiReward={} prizeCenter={}", multiRewardCount, prizeCenterCount);
        if (multiRewardCount == prizeCenterCount) {
            log.info("event=AMOUNT_CHECK_PASSED");
        } else {
            log.warn("event=AMOUNT_CHECK_MISMATCH multiReward={} prizeCenter={}", multiRewardCount, prizeCenterCount);
        }
    }

    @Override
    public void checkLastMinute() {
        boolean allMatch = true;
        List<PrizeRecord> prizeCenterList = checkScheduleIntegration.getPrizeRecordList();
        List<com.example.Multi_reward.entity.PrizeRecord> multiRewardList = prizeRecordService.queryLastMinute();

        if (prizeCenterList.size() != multiRewardList.size()) {
            log.warn("event=RECORD_SIZE_MISMATCH prizeCenter={} multiReward={}",
                    prizeCenterList.size(), multiRewardList.size());
        }

        Map<String, Integer> centerMap = new HashMap<>();
        Map<String, Integer> rewardMap = new HashMap<>();
        for (PrizeRecord r : prizeCenterList) {
            centerMap.put(r.getOutbizno(), r.getAmount());
        }
        for (com.example.Multi_reward.entity.PrizeRecord r : multiRewardList) {
            rewardMap.put(r.getOutBizNo(), r.getPrizeAmount());
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(centerMap.keySet());
        allKeys.addAll(rewardMap.keySet());

        for (String outBizNo : allKeys) {
            Integer centerAmount = centerMap.get(outBizNo);
            Integer rewardAmount = rewardMap.get(outBizNo);
            if (centerAmount == null) {
                log.warn("event=EXTRA_RECORD_IN_PRIZE_CENTER outBizNo={} amount={}", outBizNo, rewardAmount);
                allMatch = false;
                continue;
            }
            if (rewardAmount == null) {
                log.warn("event=MISSING_RECORD_IN_MULTI_REWARD outBizNo={} expectedAmount={}", outBizNo, centerAmount);
                allMatch = false;
                continue;
            }
            if (!centerAmount.equals(rewardAmount)) {
                log.warn("event=RECORD_AMOUNT_MISMATCH outBizNo={} prizeCenter={} multiReward={}",
                        outBizNo, centerAmount, rewardAmount);
                allMatch = false;
            }
        }

        if (allMatch) {
            log.info("event=LAST_MINUTE_CHECK_PASSED");
        }
    }
}