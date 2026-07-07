package com.example.Multi_reward.service;

import com.example.Multi_reward.controller.cmd.ConfigurationCmd;
import com.example.Multi_reward.entity.Configuration;

import java.util.List;

public interface ConfigurationService {
    Configuration queryByCode(String code);
    List<Configuration> queryAll();
    void add(ConfigurationCmd cmd);
    void update(String code,String rule, String description);
    void delete(String code);
}