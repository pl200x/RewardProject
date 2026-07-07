package com.example.Music_management.controller;

import com.example.Music_management.controller.cmd.RegisterCmd;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.LoginVO;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.*;
import com.example.Music_management.security.JwtUtil;
import com.example.Music_management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/register")
    public BaseVO register(@RequestBody RegisterCmd registerCmd){
        long start = System.currentTimeMillis();
        long end;
       try{
           userService.register(registerCmd);
           end = System.currentTimeMillis();
           return BaseVO.buildVO(200,end - start,true,null);
       }catch(UserInputException e){
           logger.error(e.toString());
           end = System.currentTimeMillis();
           return BaseVO.buildVO(501,end - start,false,e.getMessage());
       }catch(UserAlreadyExistException e){
           logger.error(e.toString());
           end = System.currentTimeMillis();
           return BaseVO.buildVO(502,end - start,false,e.getMessage());
       }catch(Exception e){
           logger.error(e.toString());
           end = System.currentTimeMillis();
           return BaseVO.buildVO(500,end - start,false,"other unknown error");
       }

    }
    @GetMapping("/login")
    public LoginVO login(String userName, String password){
        long start = System.currentTimeMillis();
        long end;
        try{
            User user = userService.login(userName,password);
            String token = jwtUtil.generate(user.getId(), user.getName());
            end = System.currentTimeMillis();
            return LoginVO.success(token, user.getId(), user.getName(), user.getInterest(), user.getAvatar(), end - start);
        }catch(UserInputException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return LoginVO.fail(501, end - start, e.getMessage());
        }catch(UserNotExistException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return LoginVO.fail(502, end - start, e.getMessage());
        }catch(IncorrectPasswordException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return LoginVO.fail(503, end - start, e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return LoginVO.fail(500, end - start, "other unknown error");
        }
    }
    @DeleteMapping("/logout")
    public BaseVO logout(int userId){
        long start = System.currentTimeMillis();
        long end;
        try{
            userService.logout(userId);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(503,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }
    /** 更换默认头像(0-9,存 user.avatar tinyint);头像图本体在 MinIO avatars bucket,前端按序号拼 URL */
    @PostMapping("/change-avatar")
    public BaseVO changeAvatar(int userId, int avatar){
        long start = System.currentTimeMillis();
        long end;
        try{
            userService.changeAvatar(userId, avatar);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(503,end - start,false,e.getMessage());
        }catch(UserInputException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(501,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }
    @PostMapping("/change-tag")
    public BaseVO changeTag(int userId, String interest){
        long start = System.currentTimeMillis();
        long end;
        try{
            userService.changeInterest(userId,interest);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(UserNotLoginException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(503,end - start,false,e.getMessage());
        }catch(InterestTooLongException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(501,end - start,false,e.getMessage());
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }
}
