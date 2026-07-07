package com.example.Multi_reward.controller.vo;

public class UserAnalysisPageVO {
    private BaseVO baseVO;
    private UserAnalysisVO userAnalysisVO;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public UserAnalysisVO getUserAnalysisVO() {
        return userAnalysisVO;
    }

    public void setUserAnalysisVO(UserAnalysisVO userAnalysisVO) {
        this.userAnalysisVO = userAnalysisVO;
    }

    public UserAnalysisPageVO() {
    }

    public UserAnalysisPageVO(BaseVO baseVO, UserAnalysisVO userAnalysisVO) {
        this.baseVO = baseVO;
        this.userAnalysisVO = userAnalysisVO;
    }

    @Override
    public String toString() {
        return "UserAnalysisPageVO{" +
                "baseVO=" + baseVO +
                ", userAnalysisVO=" + userAnalysisVO +
                '}';
    }
}
