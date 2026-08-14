package com.example.ai_chatbot.integration.VO;

import java.util.List;

public class MultiConfigurationPageVO {
    private List<ConfigurationVO> configurationVoList;
    private BaseVO baseVO;

    public MultiConfigurationPageVO() {
    }

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
}
