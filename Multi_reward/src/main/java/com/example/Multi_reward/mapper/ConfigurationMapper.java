package com.example.Multi_reward.mapper;

import com.example.Multi_reward.entity.Configuration;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConfigurationMapper {
    Configuration queryByCode(String code);
    void add(Configuration configuration);
    void update(String code,String rule, String description);
    void delete(String code);

    List<Configuration> queryAll();
}
