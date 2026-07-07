package com.example.Multi_reward.service;

import com.example.Multi_reward.entity.PlayRecord;

import java.util.Date;
import java.util.List;

public interface PlayRecordService {
    PlayRecord getId(int id);
    void addRecord(PlayRecord record);
    List<PlayRecord> getRecordsByDate(Date startTime, Date endTime, int userId);
}
