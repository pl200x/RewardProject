package com.example.Music_management.controller;

import com.example.Music_management.controller.converter.UserVOConverter;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.InterestedUserPageVO;
import com.example.Music_management.controller.vo.UserVO;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.IncorrectPasswordException;
import com.example.Music_management.exception.UserInputException;
import com.example.Music_management.exception.UserNotExistException;
import com.example.Music_management.repository.UserTagRepository;
import com.example.Music_management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/interest")
public class UserInterestController {
    private static final Logger logger = LoggerFactory.getLogger(MusicIntegrateController.class);
    @Autowired
    UserTagRepository userTagRepository;
    @Autowired
    UserService userService;
    @GetMapping
    public InterestedUserPageVO searchUserByInterest(String interest){
        long start = System.currentTimeMillis();
        long end;
        InterestedUserPageVO interestedUserPageVO = new InterestedUserPageVO();
        try{
            //Set<Integer> userIdSet = userTagRepository.getAllInterestedUser(interest);
            Set<User> userSet = userService.findAllUserByIds(userTagRepository.getAllInterestedUser(interest));
            Set<UserVO> userVOSet = UserVOConverter.convertToVOSet(userSet);
            end = System.currentTimeMillis();
            interestedUserPageVO.setUserVOSet(userVOSet);
            interestedUserPageVO.setBaseVO(BaseVO.buildVO(200,end - start,true,null));
            return interestedUserPageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            interestedUserPageVO.setBaseVO(BaseVO.buildVO(500,end - start,false,"other unknown error"));
            return interestedUserPageVO;
        }
    }
}
