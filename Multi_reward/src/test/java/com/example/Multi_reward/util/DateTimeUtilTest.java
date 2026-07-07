package com.example.Multi_reward.util;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void getCurrentDateStart() {
        Date start = DateTimeUtil.getCurrentDateStart();
        System.out.println(start);
    }

    @Test
    void getCurrentDateEnd() {
        Date end = DateTimeUtil.getCurrentDateEnd();
        System.out.println(end);
    }
}