package com.example.Music_management.mapper;

import com.example.Music_management.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserMapper {
    List<User> queryByIds(List<Integer> ids);
    void add(User user);
    User queryById(int id);
    User queryByName(String name);
    void updateInterest(User user);
    void updateAvatar(User user);

    Set<User> findAllUserByIds(Set<Integer> ids);
}
