package com.example.Multi_reward.controller.vo;

public class PrizeRecordCountVO {
    private BaseVO baseVO;
    private int count;

    public PrizeRecordCountVO() {
    }

    public PrizeRecordCountVO(BaseVO baseVO, int count) {
        this.baseVO = baseVO;
        this.count = count;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}