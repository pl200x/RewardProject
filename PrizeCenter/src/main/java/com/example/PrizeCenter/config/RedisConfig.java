package com.example.PrizeCenter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
    //bean有两种类型，一种是自定义的，一种是默认的从Spring Boot里面拿建好的
    //自定义的通过@mapper，@Service，@RestConttroller创建好的，系统默认的很多情况下无法直接使用，比如redistemplete他的
    //serializer未设置，无法存储对象，所以使用@Configuration和bean注解，对已有的bean进行编辑
    //@Bean是取出来RedisTemplate再放回去
}
