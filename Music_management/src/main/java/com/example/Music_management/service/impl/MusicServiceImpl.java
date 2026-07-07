package com.example.Music_management.service.impl;

import com.example.Music_management.controller.cmd.MusicCmd;
import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.User;
import com.example.Music_management.enums.MusicStatusEnum;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.repository.MusicRepository;
import com.example.Music_management.repository.UserTagRepository;
import com.example.Music_management.service.EmailService;
import com.example.Music_management.service.MusicService;
import com.example.Music_management.service.UserService;
import com.example.Music_management.service.producer.AddMusicProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MusicServiceImpl implements MusicService {
    private static final Logger logger = LoggerFactory.getLogger(MusicServiceImpl.class);

    @Autowired
    private MusicMapper musicMapper;
    @Autowired
    private UserTagRepository userTagRepository;
    @Autowired
    private MusicRankRepository musicRankRepository;
    @Autowired
    private MusicRepository musicRepository;
    @Autowired
    private AddMusicProducer addMusicProducer;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;
    @Override
    public Music queryById(int id) {
        Music music = musicRepository.getMusic(id);
        if(music == null){
            music = musicMapper.queryById(id);
            musicRepository.addMusic(id,music);
        }
        return music;
    }

    @Override
    public List<Music> queryAll() {
        return musicMapper.queryAll();
    }

    @Override
    public List<Music> getRecent(int n) {
        // 列表场景走 digest 查询(覆盖索引,不读 lyrics);详情由 /music/id 按需单查
        return musicMapper.queryRecentDigest(n);
    }

    @Override
    public List<Music> queryByName(String name) {
        // 搜索结果同为列表场景,digest 化(扫窄覆盖索引,不碰 lyrics 整行)
        return musicMapper.queryByNameDigest(name);
    }

    @Override
    @Transactional
    public void addMusic(MusicCmd musicCmd) {
        Music music = new Music();
        music.setTitle(musicCmd.getTitle());
        music.setReleaseYear(musicCmd.getReleaseYear());
        music.setArtist(musicCmd.getArtist());
        music.setTags(musicCmd.getTags());
        music.setLyrics(musicCmd.getLyrics());
        music.setStatus(MusicStatusEnum.ONLINE.getCode());
        musicMapper.addMusic(music);
//
//        Music selectedMusic =  musicMapper.queryByTitle(musicCmd.getTitle());
//        //promote to interested user
//        addMusicProducer.send(musicMapper.queryByTitle(musicCmd.getTitle());
//        recommend(music.getTitle(), musicCmd.getTags());
//        musicRankRepository.addMusicIntoRank(selectedMusic.getId());
        addMusicProducer.send(musicCmd.getTitle());
    }

    /**
     * 新歌上新通知(异步:由 Kafka 消费端调用):按 tags 找兴趣用户,邮件按"命中的曲风"个性化。
     * tags 为空 → 直接跳过邮件(进榜等其余消费动作不受影响);
     * 单封发送失败只记日志不中断,避免 Kafka 重投导致前面用户收到重复邮件。
     */
    @Override
    public void recommend(String title, String artist, String musicTags) {
        if (musicTags == null || musicTags.trim().isEmpty()) {
            logger.info("event=UPLOAD_MAIL_SKIPPED title='{}' reason=no-tags", title);
            return;
        }
        // 用户 → 命中的曲风集合(同一用户多个兴趣命中时合并)
        Map<Integer, Set<String>> matchedByUser = new HashMap<>();
        for (String rawTag : musicTags.split(",")) {
            String tag = rawTag.trim();
            if (tag.isEmpty()) continue;
            Set<Integer> userSubSet = userTagRepository.getAllInterestedUser(tag);
            if (userSubSet == null) continue;
            for (Integer uid : userSubSet) {
                matchedByUser.computeIfAbsent(uid, k -> new HashSet<>()).add(tag);
            }
        }
        if (matchedByUser.isEmpty()) {
            logger.info("event=UPLOAD_MAIL_SKIPPED title='{}' reason=no-interested-user tags='{}'", title, musicTags);
            return;
        }
        Set<User> targetUser = userService.findAllUserByIds(matchedByUser.keySet());
        int sent = 0, failed = 0;
        for (User user : targetUser) {
            String email = user.getEmail();
            if (email == null || email.trim().isEmpty()) continue;
            Set<String> matched = matchedByUser.getOrDefault(user.getId(), Set.of());
            String matchedStr = String.join(" / ", matched);
            String subject = String.format("New %s music on Music Map: %s", matchedStr, title);
            String message = String.format(
                    "Hi %s,%n%nA new %s track \"%s\" by %s has just been released on Music Map — "
                            + "it matches your interest (%s).%nCome and have a listen!%n%n— Music Map",
                    user.getName(), matchedStr, title, artist == null ? "Unknown Artist" : artist, matchedStr);
            try {
                emailService.sendEmail(email, subject, message);
                sent++;
            } catch (Exception e) {
                // 单封失败不中断整批,否则 Kafka 重投会让先收到的用户重复收信
                failed++;
                logger.warn("event=UPLOAD_MAIL_FAILED title='{}' userId={} email='{}' error={}",
                        title, user.getId(), email, e.toString());
            }
        }
        logger.info("event=UPLOAD_MAIL_DONE title='{}' tags='{}' targets={} sent={} failed={}",
                title, musicTags, matchedByUser.size(), sent, failed);
    }

    @Override
    @Transactional
    public void deleteMusic(int musicId) {
        Music music = musicMapper.queryById(musicId);
        if(music == null){
            throw new MusicNotExistException("This music is not exist");
        }
        musicMapper.updateMusicStatus(musicId,MusicStatusEnum.OFFLINE.getCode());
        musicRepository.deleteMusic(musicId);
        // 下架必须同时撤出 Redis 排行,否则 Top10/按兴趣推荐 仍会展示已删除音乐
        // (Recent/搜索由 SQL 的 status='ONLINE' 过滤,无需处理)
        musicRankRepository.removeMusicFromRank(musicId);
        if (music.getTags() != null) {
            for (String genre : music.getTags().split(",")) {
                musicRankRepository.removeFromGenreRank(musicId, genre);
            }
        }
    }
}
