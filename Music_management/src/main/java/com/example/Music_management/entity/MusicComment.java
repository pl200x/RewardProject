package com.example.Music_management.entity;

/**
 * 一条评论(Redis List 成员,经 GenericJackson2JsonRedisSerializer 序列化,暂不落 MySQL)。
 * time 为评论时刻的 epoch 毫秒,展示格式由前端决定。
 */
public class MusicComment {
    private int userId;
    private String content;
    private long time;

    public MusicComment() {
    }

    public MusicComment(int userId, String content, long time) {
        this.userId = userId;
        this.content = content;
        this.time = time;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return "MusicComment{" +
                "userId=" + userId +
                ", content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
