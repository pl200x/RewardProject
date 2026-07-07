package com.example.Music_management.service;

import com.example.Music_management.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification queryById(int id);
    List<Notification> findAllNotificationByIds(List<Integer> ids);
    List<Notification> queryByType(String type);
    List<Notification> queryAll();
    void add(Notification notification);
}
