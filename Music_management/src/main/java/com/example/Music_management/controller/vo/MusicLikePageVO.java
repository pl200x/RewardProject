package com.example.Music_management.controller.vo;

import java.util.List;

public class MusicLikePageVO {
    private int musicId;
    private List<Integer> userIds;
    /** 点赞用户摘要(去重、按点赞先后,带名字/头像),详情页头像墙用;userIds 保留做兼容 */
    private List<UserDigestVO> users;
    private BaseVO baseVO;

    public MusicLikePageVO() {
    }

    public MusicLikePageVO(int musicId, List<Integer> userIds, BaseVO baseVO) {
        this.musicId = musicId;
        this.userIds = userIds;
        this.baseVO = baseVO;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<UserDigestVO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDigestVO> users) {
        this.users = users;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    @Override
    public String toString() {
        return "MusicLikePageVO{" +
                "musicId=" + musicId +
                ", userIds=" + userIds +
                ", baseVO=" + baseVO +
                '}';
    }
}
