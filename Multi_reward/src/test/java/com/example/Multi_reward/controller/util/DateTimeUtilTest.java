package com.example.Multi_reward.controller.util;

import com.example.Multi_reward.util.DateTimeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static com.example.Multi_reward.constant.Constants.PATTERN;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DateTimeUtilTest {
    @Test
    public void testFormat(){
        Date date = new Date();
        System.out.println(DateTimeUtil.format(date,"yyyy-MM-dd HH:mm:ss:SSS"));
    }
    @Test
    public void testParse(){
        String input = "2024-05-03 10:30:00";
        System.out.println(DateTimeUtil.parse(input,PATTERN));
    }
}