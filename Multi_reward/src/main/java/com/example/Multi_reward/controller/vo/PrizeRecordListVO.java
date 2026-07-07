package com.example.Multi_reward.controller.vo;

import com.example.Multi_reward.integration.PrizeRecord;

import java.util.List;

public class PrizeRecordListVO {
    private List<PrizeRecord> prizeRecordList;
    private BaseVO baseVO;

    public PrizeRecordListVO() {
    }

    public PrizeRecordListVO(List<PrizeRecord> prizeRecordList, BaseVO baseVO) {
        this.prizeRecordList = prizeRecordList;
        this.baseVO = baseVO;
    }

    public List<PrizeRecord> getPrizeRecordList() {
        return prizeRecordList;
    }

    public void setPrizeRecordList(List<PrizeRecord> prizeRecordList) {
        this.prizeRecordList = prizeRecordList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
