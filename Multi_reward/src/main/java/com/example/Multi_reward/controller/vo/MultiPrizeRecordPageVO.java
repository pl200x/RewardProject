package com.example.Multi_reward.controller.vo;

import java.util.List;

public class MultiPrizeRecordPageVO {
    private BaseVO baseVO;
    private List<PrizeRecordVO> prizeRecordVOList;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public List<PrizeRecordVO> getPrizeRecordVOList() {
        return prizeRecordVOList;
    }

    public void setPrizeRecordVOList(List<PrizeRecordVO> prizeRecordVOList) {
        this.prizeRecordVOList = prizeRecordVOList;
    }

    @Override
    public String toString() {
        return "MultiPrizeRecordPageVO{" +
                "baseVO=" + baseVO +
                ", prizeRecordVOList=" + prizeRecordVOList +
                '}';
    }

    public MultiPrizeRecordPageVO(BaseVO baseVO, List<PrizeRecordVO> prizeRecordVOList) {
        this.baseVO = baseVO;
        this.prizeRecordVOList = prizeRecordVOList;
    }

    public MultiPrizeRecordPageVO() {
    }
}
