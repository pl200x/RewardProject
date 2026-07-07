package com.example.Multi_reward.controller.vo;

public class SingleConfigurationPageVO {
    private BaseVO baseVO;
    private ConfigurationVO configurationVO;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public ConfigurationVO getConfigurationVO() {
        return configurationVO;
    }

    public void setConfigurationVO(ConfigurationVO configurationVO) {
        this.configurationVO = configurationVO;
    }

    public SingleConfigurationPageVO() {
    }

    public SingleConfigurationPageVO(BaseVO baseVO, ConfigurationVO configurationVO) {
        this.baseVO = baseVO;
        this.configurationVO = configurationVO;
    }
}
