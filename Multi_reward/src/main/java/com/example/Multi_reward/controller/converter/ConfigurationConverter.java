package com.example.Multi_reward.controller.converter;

import com.example.Multi_reward.controller.vo.ConfigurationVO;
import com.example.Multi_reward.entity.Configuration;
import com.example.Multi_reward.entity.PlayRecord;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationConverter {
    public static ConfigurationVO convertToVO(Configuration configuration){
        ConfigurationVO configurationVO = new ConfigurationVO();
        configurationVO.setId(configuration.getId());
        configurationVO.setCode(configuration.getCode());
        configurationVO.setCode(configuration.getRule());
        configurationVO.setDescription(configuration.getDescription());
        configurationVO.setCreateTime(configuration.getCreateTime());
        configurationVO.setUpdateTime(configuration.getUpdateTime());
        return configurationVO;
    }
    public static List<ConfigurationVO> convertToVoList(List<Configuration> configurationList){
        List<ConfigurationVO> voList = new ArrayList<>();
        for(Configuration configuration : configurationList){
            voList.add(convertToVO(configuration));
        }
        return voList;
    }
}
