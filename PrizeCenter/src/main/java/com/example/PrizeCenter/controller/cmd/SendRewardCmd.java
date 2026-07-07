package com.example.PrizeCenter.controller.cmd;

public class SendRewardCmd {
    private String code;
    private int amount;
    private String outbizno;

    public SendRewardCmd() {
    }

    public SendRewardCmd(String code, int amount, String outbizno) {
        this.code = code;
        this.amount = amount;
        this.outbizno = outbizno;
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
}
