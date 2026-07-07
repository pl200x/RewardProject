package com.example.Music_management.service.impl;

import com.example.Music_management.entity.Notification;
import com.example.Music_management.exception.IncorrectNotificationException;
import com.example.Music_management.mapper.NotificationMapper;
import com.example.Music_management.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public Notification queryById(int id) {
        Notification notification = notificationMapper.queryById(id);
        if (notification == null) {
            logger.warn("Notification with id {} not found", id);
        }
        return notification;
    }

    @Override
    public List<Notification> findAllNotificationByIds(List<Integer> ids) {
        return notificationMapper.findAllNotificationByIds(ids);
    }


    @Override
    public List<Notification> queryByType(String type) {
        return notificationMapper.queryByType(type);
    }

    @Override
    public List<Notification> queryAll() {
        return notificationMapper.queryAll();
    }

    @Override
    @Transactional
    public void add(Notification notification) {
        notificationMapper.add(notification);
    }

}