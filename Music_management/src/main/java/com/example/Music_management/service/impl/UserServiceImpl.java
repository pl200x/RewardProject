package com.example.Music_management.service.impl;

import com.example.Music_management.constant.Constant;
import com.example.Music_management.controller.cmd.RegisterCmd;
import com.example.Music_management.entity.User;
import com.example.Music_management.entity.UserToken;
import com.example.Music_management.exception.*;
import com.example.Music_management.mapper.UserMapper;
import com.example.Music_management.repository.UserTagRepository;
import com.example.Music_management.repository.UserTokenRepository;
import com.example.Music_management.service.UserService;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserTagRepository userTagRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(String userName, String password) {
        if(userName == null||password ==null){
            throw new UserInputException("Please input your register information");
        }
        User user =userMapper.queryByName(userName);
        if(user == null){
            throw new UserNotExistException("This user has not register yet");
        }
        // BCrypt 比对(库里只有哈希);哈希串自带盐与 cost,matches 内部重算后恒定时间比较
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new IncorrectPasswordException("Please input correct password");
        }
        //TODO:
        UserToken userToken = buildUserToken(user);
        userTokenRepository.addUserToken(userToken.getId(),userToken);
        System.out.println("login success");
        return user;
    }

    @Override
    public void register(RegisterCmd registerCmd) {
        if(registerCmd == null){
            throw new UserInputException("Please input your register information");
        }
        if(registerCmd.getName() == null){
            throw new UserInputException("Username can't be empty");
        }
        if(registerCmd.getPassword() == null){
            throw new UserInputException("Password can't be empty");
        }
        User user = userMapper.queryByName(registerCmd.getName());
        if(user != null){
            throw new UserAlreadyExistException("Your username is already been registered");
        }
        User newUser = new User();
        newUser.setName(registerCmd.getName());
        newUser.setPassword(passwordEncoder.encode(registerCmd.getPassword()));
        newUser.setAge(registerCmd.getAge());
        newUser.setGender(registerCmd.getGender());
        newUser.setEmail(registerCmd.getEmail());
        newUser.setJob(registerCmd.getJob());
        newUser.setInterest(registerCmd.getInterest());
        // 注册随机分配一个默认头像(0-9),之后可在账户菜单更换
        newUser.setAvatar(ThreadLocalRandom.current().nextInt(Constant.AVATAR_COUNT));
        userMapper.add(newUser);
    }

    @Override
    public void logout(int userId) {
       UserToken userToken = userTokenRepository.getUserToken(userId);
       if (userToken == null){
           throw new UserNotLoginException("You have to login first to logout!");
       }
       userTokenRepository.deleteUserToken(userId);
    }

    @Override
    @Transactional
    public void changeInterest(int userId, String interest) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to change your interest!");
        }
        if (interest == null) interest = "";
        if (interest.length() > Constant.INTEREST_MAX_LENGTH) {
            // 超过 user.interest 列上限,提前抛出明确异常(否则会被 DB 的 Data-too-long 压成 unknown error)
            throw new InterestTooLongException(
                    "Interest is too long: at most " + Constant.INTEREST_MAX_LENGTH
                            + " characters allowed, but got " + interest.length());
        }
        User user = new User();
        user.setId(userId);
        user.setInterest(interest);
        userMapper.updateInterest(user);
        String[] interestArr = interest.split(",");
        for(String item: interestArr){
            userTagRepository.addInterestedUser(item,userId);
        }
    }

    @Override
    public void changeAvatar(int userId, int avatar) {
        UserToken userToken = userTokenRepository.getUserToken(userId);
        if (userToken == null){
            throw new UserNotLoginException("You have to login first to change your avatar!");
        }
        if (avatar < 0 || avatar >= Constant.AVATAR_COUNT) {
            throw new UserInputException("avatar must be between 0 and "
                    + (Constant.AVATAR_COUNT - 1) + ", but got " + avatar);
        }
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatar);
        userMapper.updateAvatar(user);
    }



    @Override
    public Set<User> findAllUserByIds(Set<Integer> userIds) {
        return userMapper.findAllUserByIds(userIds);
    }

    @Override
    public User queryById(int id) {
        return userMapper.queryById(id);
    }

    private UserToken buildUserToken(User user){
        UserToken userToken = new UserToken();
        userToken.setId(user.getId());
        userToken.setName(user.getName());
        userToken.setGender(user.getGender());
        userToken.setInterest(user.getInterest());
        return userToken;
    }
}
