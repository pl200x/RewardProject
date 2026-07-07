package com.example.Music_management.service;

import com.example.Music_management.controller.cmd.RegisterCmd;
import com.example.Music_management.entity.User;

import java.util.Set;

public interface UserService {
    User login(String userName, String password);
    void register(RegisterCmd registerCmd);

    void logout(int userId);
    void changeInterest(int userId, String interest);
    void changeAvatar(int userId, int avatar);

    Set<User> findAllUserByIds(Set<Integer> userIds);
    User queryById(int id);
}
