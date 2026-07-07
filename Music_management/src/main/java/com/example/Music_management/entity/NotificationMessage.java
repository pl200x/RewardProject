package com.example.Music_management.entity;

public class NotificationMessage {
    int userId;
    String message;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "userId=" + userId +
                ", message='" + message + '\'' +
                '}';
    }

    public NotificationMessage() {
    }
}
