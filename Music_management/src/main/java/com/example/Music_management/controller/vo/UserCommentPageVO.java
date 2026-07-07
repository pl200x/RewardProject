package com.example.Music_management.controller.vo;

import java.util.List;

public class UserCommentPageVO {
    private int userId;
    private List<Integer> musicIds;
    private long totalComment;
    private BaseVO baseVO;

    public UserCommentPageVO() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Integer> getMusicIds() {
        return musicIds;
    }

    public void setMusicIds(List<Integer> musicIds) {
        this.musicIds = musicIds;
    }

    public long getTotalComment() {
        return totalComment;
    }

    public void setTotalComment(long totalComment) {
        this.totalComment = totalComment;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
