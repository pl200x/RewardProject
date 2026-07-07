package com.example.Music_management.controller.cmd;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class PlayMusicCmd {

    private int userId;
    private int musicId;
    private String syncTime;
    private int duration;
    private String scene;
    private String code;

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

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public PlayMusicCmd() {
    }

    public PlayMusicCmd(int userId, int musicId, String syncTime, int duration, String scene, String code) {
        this.userId = userId;
        this.musicId = musicId;
        this.syncTime = syncTime;
        this.duration = duration;
        this.scene = scene;
        this.code = code;
    }
}
