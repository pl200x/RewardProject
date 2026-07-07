package com.example.Multi_reward.service.producer;

import com.example.Multi_reward.entity.RewardRetryMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.Multi_reward.constant.Constants.REWARD_RETRY_TOPIC;

@Service
public class RetryProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(RetryProducer.class);

    public void send(RewardRetryMessage retryMessage) {
        try {
            kafkaTemplate.send(REWARD_RETRY_TOPIC, objectMapper.writeValueAsString(retryMessage));
        } catch (Exception e) {
            log.error("event=RETRY_ENQUEUE_FAILED outbizno={} error={}", retryMessage.getOutbizno(), e.getMessage(), e);
        }
    }
}