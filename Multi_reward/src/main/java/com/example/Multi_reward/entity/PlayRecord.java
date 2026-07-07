package com.example.Multi_reward.entity;

import java.util.Date;

public class PlayRecord {
    private int id;
    private Date createTime;
    private Date updateTime;
    private int userId;
    private int musicId;
    private int duration;
    private Date syncTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Date syncTime) {
        this.syncTime = syncTime;
    }

    @Override
    public String toString() {
        return "PlayRecord{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", userId=" + userId +
                ", musicId=" + musicId +
                ", duration=" + duration +
                ", syncTime=" + syncTime +
                '}';
    }

    public PlayRecord(int id, Date createTime, Date updateTime, int userId, int musicId, int duration, Date syncTime) {
        this.id = id;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.userId = userId;
        this.musicId = musicId;
        this.duration = duration;
        this.syncTime = syncTime;
    }

    public PlayRecord() {
    }
}
