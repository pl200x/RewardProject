package com.example.Music_management.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class LikeRepository {
    @Autowired
    private RedisTemplate redisTemplate;
    public void addUserLikedList(int userId,int musicId){
        redisTemplate.opsForList().leftPush(buildUserLikedKey(userId),musicId);
    }
    public void addMusicLikedList(int userId,int musicId){
        redisTemplate.opsForList().leftPush(buildMusicLikedKey(musicId),userId);
    }

    public void deleteUserLikedList(int userId,int musicId){
        redisTemplate.opsForList().remove(buildUserLikedKey(userId),1L,musicId);
    }
    public void deleteMusicLikedList(int userId,int musicId){
        redisTemplate.opsForList().remove(buildMusicLikedKey(musicId),1L,userId);
    }
    public List<Integer> getUserLike(int userId){
        return redisTemplate.opsForList().range(buildUserLikedKey(userId),0,-1);
    }

    public List<Integer> getMusicLike(int musicId){
        return redisTemplate.opsForList().range(buildMusicLikedKey(musicId),0,-1);
    }
    public long getUserTotalLike(int userId){
        return  redisTemplate.opsForList().size(buildUserLikedKey(userId));
    }
    public long getMusicTotalLike(int musicId){
        return  redisTemplate.opsForList().size(buildMusicLikedKey(musicId));
    }
    private String buildUserLikedKey(int userId){
        return "userLike:" + userId;
    }
    private String buildMusicLikedKey(int musicId){
        return "musicLike:" + musicId;
    }
}
