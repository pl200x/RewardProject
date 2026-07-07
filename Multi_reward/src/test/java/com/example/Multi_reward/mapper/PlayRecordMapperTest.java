package com.example.Multi_reward.mapper;

import com.example.Multi_reward.entity.PlayRecord;
import com.example.Multi_reward.mapper.PlayRecordMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PlayRecordMapperTest {
    @Autowired
    private PlayRecordMapper playRecordMapper;
    @Test
    public void testAddRecord(){
        PlayRecord record = new PlayRecord();
        record.setMusicId(2);
        record.setUserId(3);
        record.setDuration(30);
        playRecordMapper.addRecord(record);
    }
    @Test
    public void testQueryByTime(){
        List<PlayRecord> result = playRecordMapper.queryByDate(new Date(System.currentTimeMillis()-3600000),new Date(),3);
        System.out.println(result);
    }
}