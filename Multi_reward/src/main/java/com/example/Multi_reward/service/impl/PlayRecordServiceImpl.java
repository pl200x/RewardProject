package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.entity.PlayRecord;
import com.example.Multi_reward.mapper.PlayRecordMapper;
import com.example.Multi_reward.service.PlayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PlayRecordServiceImpl implements PlayRecordService {
    @Autowired
    private PlayRecordMapper playRecordMapper;
    @Override
    public PlayRecord getId(int id) {
        return playRecordMapper.queryById(id);
    }

    @Override
    public void addRecord(PlayRecord record) {
        playRecordMapper.addRecord(record);
    }

    @Override
    public List<PlayRecord> getRecordsByDate(Date startTime, Date endTime, int userId) {
        return playRecordMapper.queryByDate(startTime,endTime,userId);
    }
}
