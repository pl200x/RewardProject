package com.example.Music_management.controller;

import com.example.Music_management.controller.cmd.MusicCmd;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.MusicLikePageVO;
import com.example.Music_management.controller.vo.UserDigestVO;
import com.example.Music_management.controller.vo.UserLikePageVO;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.UserNotLoginException;
import com.example.Music_management.service.LikeService;
import com.example.Music_management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/like")
public class LikeController {
    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);
    @Autowired
    LikeService likeService;
    @Autowired
    UserService userService;
    @PostMapping("/add")
    public BaseVO addLike(int userId,int musicId){
        long start = System.currentTimeMillis();
        long end;
        try{
            likeService.like(userId,musicId);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(501,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }
    @DeleteMapping("/delete")
    public BaseVO unLike(int userId,int musicId){
        long start = System.currentTimeMillis();
        long end;
        try {
            likeService.unlike(userId, musicId);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200, end - start, true, null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(501,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }

    }
    @GetMapping("/getuserall")
    public UserLikePageVO getUserLike(int userId){
        long start = System.currentTimeMillis();
        long end;
        UserLikePageVO userLikePageVO = new UserLikePageVO();
        try {
            userLikePageVO.setUserId(userId);
            List<Integer> userLikeList = likeService.getUserLike(userId);
            userLikePageVO.setMusicIds(userLikeList);
            userLikePageVO.setTotalLike(likeService.getUserTotalLike(userId));
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(200, end - start, true, null);
            userLikePageVO.setBaseVO(baseVO);
            return userLikePageVO;
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(501,end - start,false,e.getMessage());
            userLikePageVO.setBaseVO(baseVO);
            return userLikePageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(500,end - start,false,"other unknown error");
            userLikePageVO.setBaseVO(baseVO);
            return userLikePageVO;
        }
    }
    @GetMapping("/getmusicall")
    public MusicLikePageVO getMusicLike(int musicId){
        long start = System.currentTimeMillis();
        long end;
        MusicLikePageVO musicLikePageVO = new MusicLikePageVO();
        try {
            musicLikePageVO.setMusicId(musicId);
            List<Integer> musicLikeList = likeService.getMusicLike(musicId);
            musicLikePageVO.setUserIds(musicLikeList);
            // 头像墙:userId 去重保序后批量查用户,填充名字/头像(取消赞后列表里没有他,头像自然撤下)
            Set<Integer> distinctIds = new LinkedHashSet<>(musicLikeList == null ? List.of() : musicLikeList);
            List<UserDigestVO> users = new ArrayList<>();
            if (!distinctIds.isEmpty()) {
                Map<Integer, User> userMap = new HashMap<>();
                for (User user : userService.findAllUserByIds(distinctIds)) userMap.put(user.getId(), user);
                for (Integer uid : distinctIds) {
                    User user = userMap.get(uid);
                    if (user != null) users.add(new UserDigestVO(user.getId(), user.getName(), user.getAvatar()));
                }
            }
            musicLikePageVO.setUsers(users);
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(200, end - start, true, null);
            musicLikePageVO.setBaseVO(baseVO);
            return musicLikePageVO;
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(501,end - start,false,e.getMessage());
            musicLikePageVO.setBaseVO(baseVO);
            return musicLikePageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            BaseVO baseVO = BaseVO.buildVO(500,end - start,false,"other unknown error");
            musicLikePageVO.setBaseVO(baseVO);
            return musicLikePageVO;
        }
    }

}
