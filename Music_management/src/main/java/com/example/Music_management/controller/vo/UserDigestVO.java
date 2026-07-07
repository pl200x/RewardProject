package com.example.Music_management.controller.vo;

/** 用户轻量摘要(点赞/评论展示用):只暴露 id/名字/头像序号,不含敏感字段 */
public class UserDigestVO {
    private int userId;
    private String userName;
    private int avatar;

    public UserDigestVO() {
    }

    public UserDigestVO(int userId, String userName, int avatar) {
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
