package com.example.Multi_reward.controller.converter;

import com.example.Multi_reward.controller.vo.ConfigurationVO;
import com.example.Multi_reward.entity.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationConverterTest {

    @Test
    void convertsCodeAndRuleIntoTheirMatchingFields() {
        Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setCode("STAGE_RULE");
        configuration.setRule("stage = totalDuration < 160 ? 0 : 1");
        configuration.setDescription("Listening reward stage rule");

        ConfigurationVO result = ConfigurationConverter.convertToVO(configuration);

        assertEquals("STAGE_RULE", result.getCode());
        assertEquals("stage = totalDuration < 160 ? 0 : 1", result.getRule());
        assertEquals("Listening reward stage rule", result.getDescription());
    }
}
