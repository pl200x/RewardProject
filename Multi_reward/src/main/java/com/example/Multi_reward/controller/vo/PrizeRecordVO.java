package com.example.Multi_reward.controller.vo;

public class PrizeRecordVO {

    private int id;
    private String createTime;
    private String updateTime;
    private int userId;
    private String bizScene;
    private String prizeCode;
    private String prizeDate;
    private int prizeStage;
    private int prizeAmount;
    private String outBizNo;

    public PrizeRecordVO() {
    }

    public PrizeRecordVO(int id, String createTime, String updateTime, int userId,
                         String bizScene, String prizeCode, String prizeDate,
                         int prizeStage, int prizeAmount, String outBizNo) {
        this.id = id;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.userId = userId;
        this.bizScene = bizScene;
        this.prizeCode = prizeCode;
        this.prizeDate = prizeDate;
        this.prizeStage = prizeStage;
        this.prizeAmount = prizeAmount;
        this.outBizNo = outBizNo;
    }

    // getter & setter
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
}
