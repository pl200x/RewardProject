package com.example.Multi_reward.controller.vo;

import java.util.Date;

public class PlayRecordVO {

    private int id;
    private String createTime;
    private String updateTime;
    private int userId;
    private int musicId;
    private int duration;
    private String syncTime;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
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

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public PlayRecordVO() {
    }

    public PlayRecordVO(int id, String createTime, String updateTime, int userId, int musicId, int duration, String syncTime) {

        this.id = id;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.userId = userId;
        this.musicId = musicId;
        this.duration = duration;
        this.syncTime = syncTime;
    }
}
