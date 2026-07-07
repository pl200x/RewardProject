package com.example.Multi_reward.entity;

import java.util.Date;

public class UserAnalysis {
    private int id;
    private int userId;
    private String summaryDate;
    private int totalDuration;
    private Date lastPlayTime;
    private Date createTime;
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(String summaryDate) {
        this.summaryDate = summaryDate;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Date getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(Date lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
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

    public UserAnalysis(int id, int userId, String summaryDate, int totalDuration, Date lastPlayTime, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.summaryDate = summaryDate;
        this.totalDuration = totalDuration;
        this.lastPlayTime = lastPlayTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public UserAnalysis() {
    }

    @Override
    public String toString() {
        return "UserAnalysis{" +
                "id=" + id +
                ", userId=" + userId +
                ", summaryDate='" + summaryDate + '\'' +
                ", totalDuration=" + totalDuration +
                ", lastPlayTime=" + lastPlayTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
