package com.example.PrizeCenter.controller.vo;

import java.util.List;

public class MultiPagePrizeRecordVO {
    private List<PrizeRecordVO> prizeRecordVOList;
    private BaseVO baseVO;

    public MultiPagePrizeRecordVO() {
    }

    public MultiPagePrizeRecordVO(List<PrizeRecordVO> prizeRecordVOList, BaseVO baseVO) {
        this.prizeRecordVOList = prizeRecordVOList;
        this.baseVO = baseVO;
    }

    public List<PrizeRecordVO> getPrizeRecordVOList() {
        return prizeRecordVOList;
    }

    public void setPrizeRecordVOList(List<PrizeRecordVO> prizeRecordVOList) {
        this.prizeRecordVOList = prizeRecordVOList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
