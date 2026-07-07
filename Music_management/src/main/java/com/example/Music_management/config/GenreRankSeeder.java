package com.example.Music_management.config;

import com.example.Music_management.entity.Music;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.repository.MusicRankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把 MySQL 里的音乐灌入 Redis 排行榜:
 *  - 全局榜 musicRank:缺失才补(分 0),保留已累计的点赞分;
 *  - 各 genre 分榜 music:rank:{genre}:按歌曲 tags 拆分,分数取该歌当前全局分。
 * 这样即使还没有任何点赞/播放,首页三列(Top / Recent / 按兴趣推荐)也有数据可展示。
 * 幂等(ZADD NX),Redis/DB 未就绪时只告警不影响启动。
 */
@Component
public class GenreRankSeeder implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(GenreRankSeeder.class);

    @Autowired
    private MusicMapper musicMapper;
    @Autowired
    private MusicRankRepository musicRankRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Music> all = musicMapper.queryAll();
            if (all == null || all.isEmpty()) {
                logger.info("GenreRankSeeder: no music found, skip seeding");
                return;
            }
            int pairs = 0;
            for (Music m : all) {
                musicRankRepository.addMusicIntoRankIfAbsent(m.getId());
                if (m.getTags() == null || m.getTags().trim().isEmpty()) continue;
                double score = 0;
                try {
                    Double s = musicRankRepository.getScoreByMusicId(m.getId());
                    score = (s == null) ? 0 : s;
                } catch (Exception ignore) {
                    // 该歌尚未进全局榜时,分默认 0
                }
                for (String genre : m.getTags().split(",")) {
                    if (genre == null || genre.trim().isEmpty()) continue;
                    musicRankRepository.addToGenreRankIfAbsent(m.getId(), genre, score);
                    pairs++;
                }
            }
            logger.info("GenreRankSeeder: seeded {} music into global rank, {} (music,genre) pairs into genre ranks",
                    all.size(), pairs);
        } catch (Exception e) {
            logger.warn("GenreRankSeeder skipped (Redis/DB not ready?): {}", e.toString());
        }
    }
}
