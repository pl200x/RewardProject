package com.example.Music_management.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class UserTagRepository {
    @Autowired
    private RedisTemplate redisTemplate;
    public void addInterestedUser(String interest, int id){
        redisTemplate.opsForSet().add(buildTagKey(interest),id);
    }
    public void removeInterestedUser(String interest, int id){
        redisTemplate.opsForSet().remove(buildTagKey(interest),id);
    }
    public Set<Integer> getAllInterestedUser(String interest){
        return redisTemplate.opsForSet().members(buildTagKey(interest));
    }
    public long getAllInterestedUserCount(String interest){
        return redisTemplate.opsForSet().size(buildTagKey(interest));
    }
    private String buildTagKey(String interest){
        return "interest:" + interest;
    }

}
