package com.example.Music_management.service.impl;

import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.NotificationMessage;
import com.example.Music_management.entity.UserToken;
import com.example.Music_management.enums.NotificationTypeEnum;
import com.example.Music_management.exception.UserNotLoginException;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.mapper.UserMapper;
import com.example.Music_management.repository.LikeRepository;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.repository.UserTokenRepository;
import com.example.Music_management.service.LikeService;
import com.example.Music_management.service.NotificationService;
import com.example.Music_management.service.producer.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.Music_management.enums.NotificationTypeEnum.LIKE;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    MusicMapper musicMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserTokenRepository userTokenRepository;
    @Autowired
    MusicRankRepository musicRankRepository;
    @Autowired
    NotificationProducer notificationProducer;
    @Override
    @Transactional
    public void like(int userId, int musicId) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to logout!");
        }
        likeRepository.addUserLikedList(userId,musicId);
        likeRepository.addMusicLikedList(userId,musicId);
        musicRankRepository.updateScore(musicId,1);
        updateGenreScores(musicId, 1);
        NotificationMessage message = new NotificationMessage();
        message.setUserId(userId);
        message.setType(NotificationTypeEnum.LIKE.getCode());
        message.setMessage("User " + userId +"has liked music: " + musicId);
        notificationProducer.send(message);
    }

    @Override
    @Transactional
    public void unlike(int userId, int musicId) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to logout!");
        }
        likeRepository.deleteUserLikedList(userId,musicId);
        likeRepository.deleteMusicLikedList(userId,musicId);
        musicRankRepository.updateScore(musicId,-1);
        updateGenreScores(musicId, -1);
    }

    /** 点赞/取消赞时,同步维护该歌所属各 genre 分榜(和全局榜同增减) */
    private void updateGenreScores(int musicId, double delta){
        Music music = musicMapper.queryById(musicId);
        if (music == null || music.getTags() == null) return;
        for (String genre : music.getTags().split(",")) {
            musicRankRepository.incrGenreScore(musicId, genre, delta);
        }
    }

    @Override
    public List<Integer> getUserLike(int userId) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to logout!");
        }
        return likeRepository.getUserLike(userId);
    }

    @Override
    public List<Integer> getMusicLike(int musicId) {
        return likeRepository.getMusicLike(musicId);
    }

    @Override
    public long getUserTotalLike(int userId) {
        return likeRepository.getUserTotalLike(userId);
    }

    @Override
    public long getMusicTotalLike(int musicId) {
        return likeRepository.getMusicTotalLike(musicId);
    }
}
