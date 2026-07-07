package com.example.Multi_reward.controller.vo;

public class SinglePlayRecordPageVO {
    private BaseVO baseVO;
    private PlayRecordVO playRecordVO;

    public SinglePlayRecordPageVO() {
    }

    public SinglePlayRecordPageVO(BaseVO baseVO, PlayRecordVO playRecordVO) {
        this.baseVO = baseVO;
        this.playRecordVO = playRecordVO;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public PlayRecordVO getPlayRecordVO() {
        return playRecordVO;
    }

    public void setPlayRecordVO(PlayRecordVO playRecordVO) {
        this.playRecordVO = playRecordVO;
    }

    @Override
    public String toString() {
        return "SinglePlayRecordPageVO{" +
                "baseVO=" + baseVO +
                ", playRecordVO=" + playRecordVO +
                '}';
    }
}
