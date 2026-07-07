package com.example.PrizeCenter.controller.cmd;

import java.util.Date;

public class PrizeRecordCmd {
    private String code;
    private int amount;
    private String outbizno;
    private Date createTime;
    private Date updateTime;

    public PrizeRecordCmd() {
    }

    public PrizeRecordCmd(String code, int amount, String outbizno, Date createTime, Date updateTime) {
        this.code = code;
        this.amount = amount;
        this.outbizno = outbizno;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getOutbizno() {
        return outbizno;
    }

    public void setOutbizno(String outbizno) {
        this.outbizno = outbizno;
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
}
