package com.example.Music_management.controller.converter;

import com.example.Music_management.controller.vo.CommentVO;
import com.example.Music_management.entity.MusicComment;
import com.example.Music_management.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentVOConverter {

    public static CommentVO convertToVO(MusicComment comment){
        CommentVO commentVO = new CommentVO();
        commentVO.setUserId(comment.getUserId());
        commentVO.setContent(comment.getContent());
        commentVO.setTime(comment.getTime());
        return commentVO;
    }

    public static List<CommentVO> convertToVOList(List<MusicComment> comments){
        List<CommentVO> commentVOList = new ArrayList<>();
        for (MusicComment comment : comments) {
            commentVOList.add(convertToVO(comment));
        }
        return commentVOList;
    }

    /** 带评论者信息(userId→User,评论者已注销时名字留空、头像 0,由前端兜底展示) */
    public static List<CommentVO> convertToVOList(List<MusicComment> comments, Map<Integer, User> userMap){
        List<CommentVO> commentVOList = new ArrayList<>();
        for (MusicComment comment : comments) {
            CommentVO vo = convertToVO(comment);
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setAvatar(user.getAvatar());
            }
            commentVOList.add(vo);
        }
        return commentVOList;
    }
}
