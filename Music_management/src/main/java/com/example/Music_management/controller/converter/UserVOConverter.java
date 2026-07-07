package com.example.Music_management.controller.converter;

import com.example.Music_management.controller.vo.MusicDetailVO;
import com.example.Music_management.controller.vo.UserVO;
import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.User;

import java.util.HashSet;
import java.util.Set;

public class UserVOConverter {
    public static UserVO convertToVO(User user){
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setAge(user.getAge());
        userVO.setEmail(user.getEmail());
        userVO.setGender(user.getGender());
        userVO.setName(user.getName());
        userVO.setInterest(user.getInterest());
        userVO.setJob(user.getJob());
        return userVO;
    }
    public static Set<UserVO> convertToVOSet(Set<User> userSet){
        Set<UserVO> userVOSet = new HashSet<>();
        for(User user:userSet){
           UserVO userVO = convertToVO(user);
           userVOSet.add(userVO);
        }
        return userVOSet;
    }
}
