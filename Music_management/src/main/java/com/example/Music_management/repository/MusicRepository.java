package com.example.Music_management.repository;

import com.example.Music_management.entity.Music;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MusicRepository {

    @Autowired
    private RedisTemplate redisTemplate;

    public void addMusic(int musicId, Music music){
        redisTemplate.opsForValue().set(buildMusicKey(musicId),music);
    }
    public void deleteMusic(int musicId){
        redisTemplate.delete(buildMusicKey(musicId));
    }
    public Music getMusic(int musicId){
        return (Music) redisTemplate.opsForValue().get(buildMusicKey(musicId));
    }

    private String buildMusicKey(int musicId){
        return "musicKey:" + musicId;
    }
}
