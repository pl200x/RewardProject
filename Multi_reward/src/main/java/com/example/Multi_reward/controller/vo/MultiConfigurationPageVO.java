package com.example.Multi_reward.controller.vo;

import java.util.List;

public class MultiConfigurationPageVO {
    private List<ConfigurationVO> configurationVoList;
    private BaseVO baseVO;

    public List<ConfigurationVO> getConfigurationVoList() {
        return configurationVoList;
    }

    public void setConfigurationVoList(List<ConfigurationVO> configurationVoList) {
        this.configurationVoList = configurationVoList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public MultiConfigurationPageVO() {
    }

    public MultiConfigurationPageVO(List<ConfigurationVO> configurationVoList, BaseVO baseVO) {
        this.configurationVoList = configurationVoList;
        this.baseVO = baseVO;
    }
}
