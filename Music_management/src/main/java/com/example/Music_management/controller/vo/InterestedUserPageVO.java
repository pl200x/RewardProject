package com.example.Music_management.controller.vo;

import java.util.Set;

public class InterestedUserPageVO {
    private BaseVO baseVO;
    private Set<UserVO> userVOSet;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public Set<UserVO> getUserVOSet() {
        return userVOSet;
    }

    public void setUserVOSet(Set<UserVO> userVOSet) {
        this.userVOSet = userVOSet;
    }

    public InterestedUserPageVO(BaseVO baseVO, Set<UserVO> userVOSet) {
        this.baseVO = baseVO;
        this.userVOSet = userVOSet;
    }

    public InterestedUserPageVO() {
    }
}
