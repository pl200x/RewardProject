package com.example.Multi_reward.service;

import com.example.Multi_reward.entity.PrizeRecord;

import java.util.Date;
import java.util.List;

public interface PrizeRecordService {
    void insert(PrizeRecord prizeRecord);
    PrizeRecord selectByOutBizNo(String outBizNo);
    List<PrizeRecord> selectByUserAndDate(int userId, String prizeDate);
    int getCount();
    List<PrizeRecord> queryLastMinute();
    List<PrizeRecord> selectByUserAndDateRange(int userId, Date startTime, Date endTime);
}
