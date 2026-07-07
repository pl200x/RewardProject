package com.example.Music_management.integration;

import java.util.Date;

public class PlayRecordCmd {
    private int userId;
    private int musicId;
    private int duration;
    private String syncTime;
    private String scene;
    private String code;

    public PlayRecordCmd() {
    }

    public PlayRecordCmd(int userId, int musicId, int duration, String syncTime, String scene, String code) {
        this.userId = userId;
        this.musicId = musicId;
        this.duration = duration;
        this.syncTime = syncTime;
        this.scene = scene;
        this.code = code;
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

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}