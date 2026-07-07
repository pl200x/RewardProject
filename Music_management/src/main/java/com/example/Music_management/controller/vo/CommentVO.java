package com.example.Music_management.controller.vo;

public class CommentVO {
    private int userId;
    /** 评论者名字/头像:返回前按 userId 批量查 user 表实时填充(头像可变,不能存进评论) */
    private String userName;
    private int avatar;
    private String content;
    private long time;

    public CommentVO() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "CommentVO{" +
                "userId=" + userId +
                ", content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
