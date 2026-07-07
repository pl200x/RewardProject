package com.example.Music_management.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class MusicRankRepository {
    @Autowired
    private RedisTemplate redisTemplate;
    private final static String KEY = "musicRank";
    public void addMusicIntoRank(int musicId){
        redisTemplate.opsForZSet().add(KEY,musicId,0);
    }
    public void updateScore(int musicId,double score){
        redisTemplate.opsForZSet().incrementScore(KEY,musicId,score);
    }
    public void removeMusicFromRank(int musicId){
        redisTemplate.opsForZSet().remove(KEY,musicId);
    }
    public Set<ZSetOperations.TypedTuple<Integer>> getTopN(int topN){
        return redisTemplate.opsForZSet().reverseRangeWithScores(KEY,0,topN);
    }
    // 新歌进榜由 Kafka 消费端异步完成,详情页可能先于进榜被访问 —— 不在榜是正常状态,不能拆箱 NPE
    public double getScoreByMusicId(int musicId){
        Double score = redisTemplate.opsForZSet().score(KEY,musicId);
        return score == null ? 0 : score;
    }
    public List<Double> getScoreByMusicIds(List<Integer> musicIds){
        Integer[] array = musicIds.toArray(new Integer[0]);
        return redisTemplate.opsForZSet().score(KEY,array);
    }
    /** 榜内名次(0 为榜首);不在榜返回 -1,前端按 ranking>=0 才展示名次 */
    public long getRankByMusicId(int musicId){
        Long rank = redisTemplate.opsForZSet().reverseRank(KEY,musicId);
        return rank == null ? -1 : rank;
    }

    // ==== 按曲风(genre)的分榜:每个 genre 一个 ZSet,key = "music:rank:{genre}" ====
    // 千万级用户下,每个用户兴趣各异,无法用单一共享缓存;而每次查 MySQL 又扛不住。
    // 折中:一个 genre 一个 Redis ZSet(genre→musicId/score),用户多兴趣时读多个再合并成个性化 TopN。
    private final static String GENRE_KEY_PREFIX = "music:rank:";
    private String genreKey(String genre){
        return GENRE_KEY_PREFIX + genre.trim();
    }
    /** 把某首歌写入某 genre 分榜(设定绝对分,用于启动灌种子) */
    public void addToGenreRank(int musicId, String genre, double score){
        if (genre == null || genre.trim().isEmpty()) return;
        redisTemplate.opsForZSet().add(genreKey(genre), musicId, score);
    }
    /** 某 genre 分榜内加/减分(点赞 +1 / 取消 -1) */
    public void incrGenreScore(int musicId, String genre, double delta){
        if (genre == null || genre.trim().isEmpty()) return;
        redisTemplate.opsForZSet().incrementScore(genreKey(genre), musicId, delta);
    }
    public void removeFromGenreRank(int musicId, String genre){
        if (genre == null || genre.trim().isEmpty()) return;
        redisTemplate.opsForZSet().remove(genreKey(genre), musicId);
    }
    /** 取某 genre 分榜的 TopN(带分数) */
    public Set<ZSetOperations.TypedTuple<Integer>> getTopNByGenre(String genre, int n){
        if (genre == null || genre.trim().isEmpty()) return null;
        return redisTemplate.opsForZSet().reverseRangeWithScores(genreKey(genre), 0, n - 1);
    }

    // ==== 仅当成员不存在时写入(ZADD NX):启动灌种子用,不覆盖线上已累计的分 ====
    public void addMusicIntoRankIfAbsent(int musicId){
        redisTemplate.opsForZSet().addIfAbsent(KEY, musicId, 0);
    }
    public void addToGenreRankIfAbsent(int musicId, String genre, double score){
        if (genre == null || genre.trim().isEmpty()) return;
        redisTemplate.opsForZSet().addIfAbsent(genreKey(genre), musicId, score);
    }
}
