package com.example.Multi_reward.entity;

import com.example.Multi_reward.controller.cmd.PlayCmd;

public class RewardRetryMessage {
    //PlayCmd playCmd, int stage, int amount,String today,String outbizno
    private int stage;
    private int amount;
    private String today;
    private String outbizno;
    private PlayCmd playCmd;

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getOutbizno() {
        return outbizno;
    }

    public void setOutbizno(String outbizno) {
        this.outbizno = outbizno;
    }

    public PlayCmd getPlayCmd() {
        return playCmd;
    }

    public void setPlayCmd(PlayCmd playCmd) {
        this.playCmd = playCmd;
    }

    public RewardRetryMessage() {
    }

    public RewardRetryMessage(int stage, int amount, String today, String outbizno, PlayCmd playCmd) {
        this.stage = stage;
        this.amount = amount;
        this.today = today;
        this.outbizno = outbizno;
        this.playCmd = playCmd;
    }
}
