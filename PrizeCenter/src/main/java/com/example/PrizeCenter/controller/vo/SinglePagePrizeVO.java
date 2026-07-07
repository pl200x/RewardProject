package com.example.PrizeCenter.controller.vo;

public class SinglePagePrizeVO {
    private PrizeVO prizeVO;
    private BaseVO baseVO;

    public SinglePagePrizeVO() {
    }

    public SinglePagePrizeVO(PrizeVO prizeVO, BaseVO baseVO) {
        this.prizeVO = prizeVO;
        this.baseVO = baseVO;
    }

    public PrizeVO getPrizeVO() {
        return prizeVO;
    }

    public void setPrizeVO(PrizeVO prizeVO) {
        this.prizeVO = prizeVO;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
