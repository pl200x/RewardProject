package com.example.Music_management.repository;

import com.example.Music_management.entity.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class UserTokenRepository{
    @Autowired
    private RedisTemplate redisTemplate;
    public void addUserToken(int userId, UserToken userToken){
        redisTemplate.opsForValue().set(buildUserTokenKey(userId),userToken,10L, TimeUnit.MINUTES);

    }
    public UserToken getUserToken(int userId){
        return (UserToken) redisTemplate.opsForValue().get(buildUserTokenKey(userId));
    }
    public void deleteUserToken(int userId){
        redisTemplate.delete(buildUserTokenKey(userId));
    }
    private String buildUserTokenKey(int userId){
        return "token:" + userId;
    }

}
