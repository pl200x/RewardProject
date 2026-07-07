package com.example.Multi_reward.controller.cmd;

import java.util.Date;

public class ConfigurationCmd {
    private String code;
    private String rule;
    private String description;

    public ConfigurationCmd() {
    }

    public ConfigurationCmd(String code, String rule, String description) {
        this.code = code;
        this.rule = rule;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
