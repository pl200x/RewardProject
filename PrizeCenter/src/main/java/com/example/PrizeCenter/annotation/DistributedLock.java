package com.example.PrizeCenter.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock{
    String prefix() default "lock:";
    // SpEL 表达式，引用方法参数用 #paramName 语法，静态字符串用 'value' 语法
    String key();
    // 0 表示抢不到锁立即失败，不阻塞线程
    long waitTime() default 0;
    // -1 启用 Redisson watchdog 自动续期，避免业务未完成时锁提前到期
    long leaseTime() default -1;
    TimeUnit unit() default TimeUnit.SECONDS;
}