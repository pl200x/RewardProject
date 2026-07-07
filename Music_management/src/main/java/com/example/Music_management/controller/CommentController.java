package com.example.Music_management.controller;

import com.example.Music_management.controller.converter.CommentVOConverter;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.MusicCommentPageVO;
import com.example.Music_management.controller.vo.UserCommentPageVO;
import com.example.Music_management.entity.MusicComment;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.InvalidCommentException;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.exception.UserNotLoginException;
import com.example.Music_management.service.CommentService;
import com.example.Music_management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论(Redis List,暂不落 MySQL):发评论需登录;读歌曲评论公开(游客可见)。
 * 首评给排行榜 +2(全局 + genre),同一用户对同一首歌重复评论只存内容不重复加分。
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;

    /** 按评论里的 userId 批量查用户(去重一次 IN 查询),供 VO 填充名字/头像 */
    private Map<Integer, User> loadUserMap(List<MusicComment> comments){
        Map<Integer, User> userMap = new HashMap<>();
        Set<Integer> userIds = new HashSet<>();
        for (MusicComment comment : comments) userIds.add(comment.getUserId());
        if (!userIds.isEmpty()) {
            for (User user : userService.findAllUserByIds(userIds)) userMap.put(user.getId(), user);
        }
        return userMap;
    }

    @PostMapping("/add")
    public BaseVO addComment(int userId, int musicId, String content){
        long start = System.currentTimeMillis();
        long end;
        try{
            commentService.addComment(userId, musicId, content);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(501,end - start,false,e.getMessage());
        }catch(InvalidCommentException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(502,end - start,false,e.getMessage());
        }catch(MusicNotExistException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(503,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }

    @GetMapping("/getmusicall")
    public MusicCommentPageVO getMusicComments(int musicId){
        long start = System.currentTimeMillis();
        long end;
        MusicCommentPageVO musicCommentPageVO = new MusicCommentPageVO();
        try {
            musicCommentPageVO.setMusicId(musicId);
            List<MusicComment> comments = commentService.getMusicComments(musicId);
            musicCommentPageVO.setComments(CommentVOConverter.convertToVOList(comments, loadUserMap(comments)));
            musicCommentPageVO.setTotalComment(commentService.getMusicTotalComment(musicId));
            end = System.currentTimeMillis();
            musicCommentPageVO.setBaseVO(BaseVO.buildVO(200, end - start, true, null));
            return musicCommentPageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            musicCommentPageVO.setBaseVO(BaseVO.buildVO(500,end - start,false,"other unknown error"));
            return musicCommentPageVO;
        }
    }

    @GetMapping("/getuserall")
    public UserCommentPageVO getUserComments(int userId){
        long start = System.currentTimeMillis();
        long end;
        UserCommentPageVO userCommentPageVO = new UserCommentPageVO();
        try {
            userCommentPageVO.setUserId(userId);
            userCommentPageVO.setMusicIds(commentService.getUserComment(userId));
            userCommentPageVO.setTotalComment(commentService.getUserTotalComment(userId));
            end = System.currentTimeMillis();
            userCommentPageVO.setBaseVO(BaseVO.buildVO(200, end - start, true, null));
            return userCommentPageVO;
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            userCommentPageVO.setBaseVO(BaseVO.buildVO(501,end - start,false,e.getMessage()));
            return userCommentPageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            userCommentPageVO.setBaseVO(BaseVO.buildVO(500,end - start,false,"other unknown error"));
            return userCommentPageVO;
        }
    }
}
