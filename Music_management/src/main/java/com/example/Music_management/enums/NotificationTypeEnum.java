package com.example.Music_management.enums;

public enum NotificationTypeEnum {
    LIKE("LIKE","Music has been liked");
    private String code;
    private String desc;
    NotificationTypeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public String getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}
