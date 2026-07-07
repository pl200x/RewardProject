package com.example.Multi_reward.service.consumer;

import com.example.Multi_reward.entity.RewardRetryMessage;
import com.example.Multi_reward.exception.KafkaMessageDeserializeException;
import com.example.Multi_reward.service.SyncService;
import com.example.Multi_reward.util.LogContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.Multi_reward.constant.Constants.REWARD_RETRY_TOPIC;

/**
 * 发奖重试消费者:主流程调 PrizeCenter 失败后消息入队,这里再次尝试发奖。
 * 业务异常不在此捕获——抛出后由 KafkaErrorConfig 的错误处理器原地重试,
 * 重试耗尽发布到 REWARD_RETRY_TOPIC.DLT 死信队列,消息不丢。
 */
@Service
public class RetryConsumer {

    @Autowired
    private SyncService syncService;
    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(RetryConsumer.class);

    @KafkaListener(topics = REWARD_RETRY_TOPIC, groupId = "sync-group")
    public void listen(String message) {
        RewardRetryMessage retryMessage;
        try {
            retryMessage = objectMapper.readValue(message, RewardRetryMessage.class);
        } catch (Exception e) {
            log.warn("event=RETRY_DESERIALIZE_FAILED error={}", e.getMessage(), e);
            throw new KafkaMessageDeserializeException("bad reward retry message: " + e.getMessage());
        }
        try {
            LogContext.setTraceId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            if (retryMessage.getPlayCmd() != null) {
                LogContext.setUserId(retryMessage.getPlayCmd().getUserId());
            }
            log.info("event=RETRY_ATTEMPT outbizno={}", retryMessage.getOutbizno());
            syncService.sendPrize(retryMessage);
        } catch (DuplicateKeyException e) {
            // 幂等唯一键命中说明奖已发过,业务上等同成功,不再重试也不进死信
            log.info("event=PRIZE_DUPLICATE_SKIPPED outbizno={}", retryMessage.getOutbizno());
        } finally {
            LogContext.clear();
        }
    }
}
