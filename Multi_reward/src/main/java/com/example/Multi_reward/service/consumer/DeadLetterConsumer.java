package com.example.Multi_reward.service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static com.example.Multi_reward.constant.Constants.REWARD_RETRY_DLT_TOPIC;

/**
 * 死信队列消费者:重试耗尽或不可自愈的发奖消息最终落到这里,
 * 带着原 topic 与异常信息记 ERROR 日志留证据,保证可排查、可回放。
 * 演示项目只记日志;生产环境可在此落库并接告警。
 */
@Service
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @KafkaListener(topics = REWARD_RETRY_DLT_TOPIC, groupId = "sync-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.error("event=DLQ_RECEIVED originalTopic={} error={} message={}",
                headerAsString(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                headerAsString(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                record.value());
    }

    private String headerAsString(ConsumerRecord<String, String> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
