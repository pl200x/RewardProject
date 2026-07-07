package com.example.Music_management.repository;

import com.example.Music_management.entity.MusicComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论存储(全 Redis,结构对齐 LikeRepository 的双向 List):
 *   musicComment:{musicId}        List<MusicComment>  歌曲的评论(新评论在头部)
 *   userComment:{userId}          List<Integer>       用户评论过的 musicId(可重复,保留每次评论)
 *   musicCommentScored:{musicId}  Set<Integer>        已为该歌加过分的 userId —— 加分幂等守卫
 */
@Repository
public class CommentRepository {
    @Autowired
    private RedisTemplate redisTemplate;

    public void addMusicCommentList(int musicId, MusicComment comment){
        redisTemplate.opsForList().leftPush(buildMusicCommentKey(musicId), comment);
    }
    public void addUserCommentList(int userId, int musicId){
        redisTemplate.opsForList().leftPush(buildUserCommentKey(userId), musicId);
    }
    public List<MusicComment> getMusicComments(int musicId){
        return redisTemplate.opsForList().range(buildMusicCommentKey(musicId),0,-1);
    }
    public List<Integer> getUserComment(int userId){
        return redisTemplate.opsForList().range(buildUserCommentKey(userId),0,-1);
    }
    public long getMusicTotalComment(int musicId){
        return redisTemplate.opsForList().size(buildMusicCommentKey(musicId));
    }
    public long getUserTotalComment(int userId){
        return redisTemplate.opsForList().size(buildUserCommentKey(userId));
    }
    /**
     * 首评守卫:SADD 原子写入,只有第一次(真正加入集合)返回 true。
     * 调用方仅在 true 时给排行榜 +2,保证同一用户对同一首歌重复/并发评论只加一次分。
     */
    public boolean markScoredIfFirst(int musicId, int userId){
        Long added = redisTemplate.opsForSet().add(buildScoredKey(musicId), userId);
        return added != null && added == 1;
    }

    private String buildMusicCommentKey(int musicId){
        return "musicComment:" + musicId;
    }
    private String buildUserCommentKey(int userId){
        return "userComment:" + userId;
    }
    private String buildScoredKey(int musicId){
        return "musicCommentScored:" + musicId;
    }
}
