package com.example.Multi_reward.entity;

import java.util.Date;

public class PrizeRecord {
    private int id;
    private int userId;
    private String bizScene;
    private String prizeCode;
    private String prizeDate;
    private int prizeStage;
    private int prizeAmount;
    private String outBizNo;
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

    public String getBizScene() {
        return bizScene;
    }

    public void setBizScene(String bizScene) {
        this.bizScene = bizScene;
    }

    public String getPrizeCode() {
        return prizeCode;
    }

    public void setPrizeCode(String prizeCode) {
        this.prizeCode = prizeCode;
    }

    public String getPrizeDate() {
        return prizeDate;
    }

    public void setPrizeDate(String prizeDate) {
        this.prizeDate = prizeDate;
    }

    public int getPrizeStage() {
        return prizeStage;
    }

    public void setPrizeStage(int prizeStage) {
        this.prizeStage = prizeStage;
    }

    public int getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(int prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public String getOutBizNo() {
        return outBizNo;
    }

    public void setOutBizNo(String outBizNo) {
        this.outBizNo = outBizNo;
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

    public void setUpdateTIme(Date updateTime) {
        this.updateTime = updateTime;
    }

    public PrizeRecord(int id, int userId, String bizScene, String prizeCode, String prizeDate, int prizeStage, int prizeAmount, String outBizNo, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.bizScene = bizScene;
        this.prizeCode = prizeCode;
        this.prizeDate = prizeDate;
        this.prizeStage = prizeStage;
        this.prizeAmount = prizeAmount;
        this.outBizNo = outBizNo;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public PrizeRecord() {
    }

    @Override
    public String toString() {
        return "prizeRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", bizScene='" + bizScene + '\'' +
                ", prizeCode='" + prizeCode + '\'' +
                ", prizeDate='" + prizeDate + '\'' +
                ", prizeStage=" + prizeStage +
                ", prizeAmount=" + prizeAmount +
                ", outBizNO='" + outBizNo + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
