package com.example.Multi_reward.controller.vo;

import java.util.Date;

public class ConfigurationVO {
    private int id;
    private String code;
    private String rule;
    private String description;
    private Date createTime;
    private Date updateTime;

    public ConfigurationVO() {
    }

    public ConfigurationVO(int id, String code, String rule, String description, Date createTime, Date updateTime) {
        this.id = id;
        this.code = code;
        this.rule = rule;
        this.description = description;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
