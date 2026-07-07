package com.example.Music_management.mapper;

import com.example.Music_management.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testAddUser() {
        User user = new User("aa", "111", 23, 1, "aaEmail@123.com", "java", "coding");
        userMapper.add(user);
    }

    @Test
    public void testQueryById() {
        System.out.println(userMapper.queryById(1));
    }

}