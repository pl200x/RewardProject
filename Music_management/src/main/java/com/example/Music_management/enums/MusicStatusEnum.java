package com.example.Music_management.enums;

public enum MusicStatusEnum {
    ONLINE("ONLINE","Music is online to everyone"),
    OFFLINE("OFFLINE","Music now is unavilable");
    private String code;
    private String desc;
    MusicStatusEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
