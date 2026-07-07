package com.example.Music_management.service.impl;

import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.MusicComment;
import com.example.Music_management.entity.UserToken;
import com.example.Music_management.exception.InvalidCommentException;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.exception.UserNotLoginException;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.repository.CommentRepository;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.repository.UserTokenRepository;
import com.example.Music_management.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
    /** 单条评论长度上限(字符) */
    private static final int MAX_COMMENT_CHARS = 300;
    /** 首评加分(只加一次,由 musicCommentScored 集合守卫) */
    private static final double FIRST_COMMENT_SCORE = 2;

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    MusicMapper musicMapper;
    @Autowired
    UserTokenRepository userTokenRepository;
    @Autowired
    MusicRankRepository musicRankRepository;

    @Override
    public void addComment(int userId, int musicId, String content) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to comment!");
        }
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()){
            throw new InvalidCommentException("comment content is required");
        }
        if (trimmed.length() > MAX_COMMENT_CHARS){
            throw new InvalidCommentException("comment is too long: at most " + MAX_COMMENT_CHARS
                    + " characters, but got " + trimmed.length());
        }
        // 直查 mapper 确认歌曲存在(不走 queryById 的缓存路径,避免给不存在的 id 留缓存副作用)
        Music music = musicMapper.queryById(musicId);
        if (music == null){
            throw new MusicNotExistException("the music you are trying to comment is not exist!");
        }

        commentRepository.addMusicCommentList(musicId, new MusicComment(userId, trimmed, System.currentTimeMillis()));
        commentRepository.addUserCommentList(userId, musicId);

        // 首评 +2(全局榜 + genre 分榜同步);重复评论只存内容不加分,SADD 原子保证并发下也只加一次
        boolean scored = commentRepository.markScoredIfFirst(musicId, userId);
        if (scored){
            musicRankRepository.updateScore(musicId, FIRST_COMMENT_SCORE);
            if (music.getTags() != null){
                for (String genre : music.getTags().split(",")) {
                    musicRankRepository.incrGenreScore(musicId, genre, FIRST_COMMENT_SCORE);
                }
            }
        }
        logger.info("event=COMMENT_ADDED musicId={} userId={} scored={} length={}",
                musicId, userId, scored, trimmed.length());
    }

    @Override
    public List<MusicComment> getMusicComments(int musicId) {
        List<MusicComment> comments = commentRepository.getMusicComments(musicId);
        return comments == null ? new ArrayList<>() : comments;
    }

    @Override
    public long getMusicTotalComment(int musicId) {
        return commentRepository.getMusicTotalComment(musicId);
    }

    @Override
    public List<Integer> getUserComment(int userId) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to comment!");
        }
        List<Integer> musicIds = commentRepository.getUserComment(userId);
        return musicIds == null ? new ArrayList<>() : musicIds;
    }

    @Override
    public long getUserTotalComment(int userId) {
        return commentRepository.getUserTotalComment(userId);
    }
}
