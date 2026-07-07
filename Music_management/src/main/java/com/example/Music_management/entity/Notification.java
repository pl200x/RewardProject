package com.example.Music_management.entity;

import java.util.Date;

public class Notification {
    private int id;
    private String type;
    private String detail;
    private Date eventTime;
    private String sender;
    private String receiver;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Notification(int id, String type, String detail, Date eventTime, String sender, String receiver) {
        this.id = id;
        this.type = type;
        this.detail = detail;
        this.eventTime = eventTime;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Notification() {
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", detail='" + detail + '\'' +
                ", eventTime=" + eventTime +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
