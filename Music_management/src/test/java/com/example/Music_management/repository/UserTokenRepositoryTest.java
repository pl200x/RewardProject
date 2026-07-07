package com.example.Music_management.repository;

import com.example.Music_management.entity.UserToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserTokenRepositoryTest {
    @Autowired
    UserTokenRepository userTokenRepository = new UserTokenRepository();
    @Test
    public void testAddToken(){
        UserToken userToken = new UserToken();
        userToken.setId(1);
        userToken.setGender(0);
        userToken.setName("aa4");
        userToken.setInterest("a,b,c");
        userTokenRepository.addUserToken(1,userToken);
    }
    @Test
    public void testGetToken(){
        UserToken userToken = userTokenRepository.getUserToken(1);
        System.out.println(userToken);
    }
}