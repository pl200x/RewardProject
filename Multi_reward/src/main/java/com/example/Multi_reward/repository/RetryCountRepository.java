package com.example.Multi_reward.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RetryCountRepository {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public void setRetryCount(String outbizno, int time){
        redisTemplate.opsForValue().set(buildRetryKey(outbizno),String.valueOf(time),24,TimeUnit.HOURS);
    }
    private String buildRetryKey(String outbizno){
        return outbizno + "_retry";
    }
    public int get(String outbizno){
        String time = redisTemplate.opsForValue().get(buildRetryKey(outbizno));
        if(time == null){
            return 0;
        }else{
            return Integer.parseInt(time);
        }
    }
}
