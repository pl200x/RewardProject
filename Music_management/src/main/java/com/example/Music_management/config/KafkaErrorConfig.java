package com.example.Music_management.config;

import com.example.Music_management.exception.IncorrectNotificationException;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.exception.UserNotExistException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static com.example.Music_management.constant.Constant.DLT_SUFFIX;

/**
 * Kafka 消费失败兜底:原地重试 2 次(间隔 2s),仍失败则把消息连同异常信息
 * 发布到 <原topic>.DLT 死信队列(由 DeadLetterConsumer 记录),消息不丢。
 * 消息损坏/数据不存在这类重试不可能自愈的异常不重试,直接进死信。
 * Boot 自动装配会把本 CommonErrorHandler 挂到默认的 ListenerContainerFactory。
 */
@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // 显式指定死信 topic 后缀,与 DeadLetterConsumer 订阅的常量对齐
        // (spring-kafka 各版本默认后缀不一致,.DLT / -dlt 都出现过,不能依赖默认值)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + DLT_SUFFIX, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2L));
        errorHandler.addNotRetryableExceptions(
                IncorrectNotificationException.class,
                MusicNotExistException.class,
                UserNotExistException.class);
        return errorHandler;
    }
}
