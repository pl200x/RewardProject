package com.example.Multi_reward.exception;

/**
 * Kafka 消息体反序列化失败(毒丸消息)。重试不可能自愈,
 * 在 KafkaErrorConfig 中登记为不可重试异常,直接进死信队列。
 */
public class KafkaMessageDeserializeException extends RuntimeException {
    public KafkaMessageDeserializeException(String message) {
        super(message);
    }
}
