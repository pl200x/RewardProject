package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.entity.PrizeRecord;
import com.example.Multi_reward.mapper.PrizeRecordMapper;
import com.example.Multi_reward.service.PrizeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeRecordServiceImpl implements PrizeRecordService {
    @Autowired
    private PrizeRecordMapper prizeRecordMapper;
    @Override
    public void insert(PrizeRecord prizeRecord) {
        prizeRecordMapper.insert(prizeRecord);
    }

    @Override
    public PrizeRecord selectByOutBizNo(String outBizNo) {
        return prizeRecordMapper.selectByOutBizNo(outBizNo);
    }

    @Override
    public List<PrizeRecord> selectByUserAndDate(int userId, String prizeDate) {
        return prizeRecordMapper.selectByUserAndDate(userId, prizeDate);
    }

    @Override
    public int getCount() {
        return prizeRecordMapper.getCount();
    }

    @Override
    public List<PrizeRecord> queryLastMinute() {
        return prizeRecordMapper.getLastMinute();
    }
}
