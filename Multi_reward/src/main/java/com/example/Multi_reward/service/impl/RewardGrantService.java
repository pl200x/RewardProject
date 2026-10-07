package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.entity.RewardRetryMessage;
import com.example.Multi_reward.integration.PrizeIntegration;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Separate proxy boundary used by both normal playback and Kafka redelivery. */
@Service
public class RewardGrantService {
    private static final Logger log = LoggerFactory.getLogger(RewardGrantService.class);
    private final PrizeIntegration prizeIntegration;

    public RewardGrantService(PrizeIntegration prizeIntegration) {
        this.prizeIntegration = prizeIntegration;
    }

    @GlobalTransactional(name = "reward-send-prize", rollbackFor = Exception.class, timeoutMills = 15000)
    @Transactional(rollbackFor = Exception.class)
    public void sendPrize(RewardRetryMessage message) {
        log.info("event=REWARD_GLOBAL_TRANSACTION xid={} outbizno={}", RootContext.getXID(), message.getOutbizno());
        // Let exceptions escape BOTH transaction interceptors before enqueueing a retry.
        prizeIntegration.sendPrize(message.getPlayCmd(), message.getStage(), message.getAmount(),
                message.getToday(), message.getOutbizno());
    }
}
