package com.example.Multi_reward.constant;

public class Constants {
    public final static String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static String PRIZE_STAGE_RULE_CODE = "STAGE_RULE";
    public final static String PRIZE_AMOUNT_RULE_CODE = "AMOUNT_RULE";
    public final static String REWARD_RETRY_TOPIC = "REWARD_RETRY_TOPIC";
    /** 死信 topic 后缀(KafkaErrorConfig 发布与 DeadLetterConsumer 订阅共用,显式指定不依赖框架默认) */
    public final static String DLT_SUFFIX = ".DLT";
    /** 死信队列:重试耗尽/不可自愈的发奖消息落入此 topic */
    public final static String REWARD_RETRY_DLT_TOPIC = REWARD_RETRY_TOPIC + DLT_SUFFIX;
}
