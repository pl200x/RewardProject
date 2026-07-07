package com.example.Music_management.constant;

public class Constant {
    public static final String MUSIC_TOPIC = "MUSIC";
    public static final String NOTIFICATION = "NOTIFICATION";
    /** 死信 topic 后缀(KafkaErrorConfig 发布与 DeadLetterConsumer 订阅共用,显式指定不依赖框架默认) */
    public static final String DLT_SUFFIX = ".DLT";
    /** 死信队列:消费重试耗尽/不可自愈的消息落入对应 .DLT topic */
    public static final String MUSIC_DLT_TOPIC = MUSIC_TOPIC + DLT_SUFFIX;
    public static final String NOTIFICATION_DLT_TOPIC = NOTIFICATION + DLT_SUFFIX;
    /** user.interest 列(varchar(255))的字符上限,兴趣标签拼接串不得超过 */
    public static final int INTEREST_MAX_LENGTH = 255;
    /** 默认头像个数(MinIO avatars bucket 的 0.svg ~ 9.svg),user.avatar 取值 0 ~ AVATAR_COUNT-1 */
    public static final int AVATAR_COUNT = 10;
}
