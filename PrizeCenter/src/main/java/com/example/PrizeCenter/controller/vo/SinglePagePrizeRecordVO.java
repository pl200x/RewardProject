package com.example.PrizeCenter.controller.vo;


public class SinglePagePrizeRecordVO {
    private BaseVO baseVO;
    private PrizeRecordVO prizeRecordVO;

    public SinglePagePrizeRecordVO() {
    }

    public SinglePagePrizeRecordVO(BaseVO baseVO, PrizeRecordVO prizeRecordVO) {
        this.baseVO = baseVO;
        this.prizeRecordVO = prizeRecordVO;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public PrizeRecordVO getPrizeRewardVO() {
        return prizeRecordVO;
    }

    public void setPrizeRewardVO(PrizeRecordVO prizeRecordVO) {
        this.prizeRecordVO = prizeRecordVO;
    }
}
