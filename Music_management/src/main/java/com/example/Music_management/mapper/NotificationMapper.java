package com.example.Music_management.mapper;

import com.example.Music_management.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    Notification queryById(int id);
    List<Notification> findAllNotificationByIds(List<Integer> ids);
    List<Notification> queryByType(String type);
    List<Notification> queryAll();
    void add(Notification notification);

}
