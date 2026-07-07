package com.example.PrizeCenter.entity;

import java.util.Date;

public class Prize {

    private int id;
    private String code;
    private String name;
    private String description;
    private int storage;
    private Date createTime;
    private Date updateTime;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
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

    public Prize() {
    }

    public Prize(int id, String code, String name, String description, int storage, Date createTime, Date updateTime) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.storage = storage;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
