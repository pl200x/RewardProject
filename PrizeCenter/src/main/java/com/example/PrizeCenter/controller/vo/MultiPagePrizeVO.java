package com.example.PrizeCenter.controller.vo;

import java.util.List;

public class MultiPagePrizeVO {
    private List<PrizeVO> prizeVOList;
    private BaseVO baseVO;

    public MultiPagePrizeVO() {
    }

    public MultiPagePrizeVO(List<PrizeVO> prizeVOList, BaseVO baseVO) {
        this.prizeVOList = prizeVOList;
        this.baseVO = baseVO;
    }

    public List<PrizeVO> getPrizeVOList() {
        return prizeVOList;
    }

    public void setPrizeVOList(List<PrizeVO> prizeVOList) {
        this.prizeVOList = prizeVOList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
