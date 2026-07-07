package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.controller.cmd.ConfigurationCmd;
import com.example.Multi_reward.entity.Configuration;
import com.example.Multi_reward.exception.ConfigurationNotExistException;
import com.example.Multi_reward.mapper.ConfigurationMapper;
import com.example.Multi_reward.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    private ConfigurationMapper configurationMapper;
    @Override
    public Configuration queryByCode(String code) {
        return configurationMapper.queryByCode(code);
    }

    @Override
    public List<Configuration> queryAll() {
        return configurationMapper.queryAll();
    }

    @Override
    public void add(ConfigurationCmd cmd) {
        Configuration configuration = new Configuration();
        configuration.setCode(cmd.getCode());
        configuration.setDescription(cmd.getDescription());
        configuration.setRule(cmd.getRule());
        configurationMapper.add(configuration);
    }

    @Override
    public void update(String code, String rule, String description) {
        Configuration configuration = configurationMapper.queryByCode(code);
        if(configuration == null){
            throw new ConfigurationNotExistException("this exception is not exist");
        }
        configurationMapper.update(code,rule,description);
    }

    @Override
    public void delete(String code) {
        configurationMapper.delete(code);
    }
}
