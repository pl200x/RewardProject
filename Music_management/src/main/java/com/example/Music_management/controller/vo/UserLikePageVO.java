package com.example.Music_management.controller.vo;

import java.util.List;

public class UserLikePageVO {
    private int userId;
    private List<Integer> musicIds;
    private BaseVO baseVO;
    private long totalLike;

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

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }


    public UserLikePageVO(int userId, List<Integer> musicIds, BaseVO baseVO, long totalLike) {
        this.userId = userId;
        this.musicIds = musicIds;
        this.baseVO = baseVO;
        this.totalLike = totalLike;
    }

    public UserLikePageVO() {
    }

    @Override
    public String toString() {
        return "UserLikePageVO{" +
                "userId=" + userId +
                ", musicIds=" + musicIds +
                ", baseVO=" + baseVO +
                ", totalLike=" + totalLike +
                '}';
    }

    public long getTotalLike() {
        return totalLike;
    }

    public void setTotalLike(long totalLike) {
        this.totalLike = totalLike;
    }
}
