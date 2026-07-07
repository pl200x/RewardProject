package com.example.Multi_reward.service.impl;

import com.example.Multi_reward.entity.UserAnalysis;
import com.example.Multi_reward.mapper.UserAnalysisMapper;
import com.example.Multi_reward.service.UserAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAnalysisServiceImpl implements UserAnalysisService {
    @Autowired
    private UserAnalysisMapper userAnalysisMapper;
    @Override
    public void insert(UserAnalysis userAnalysis) {
        userAnalysisMapper.insert(userAnalysis);
    }

    @Override
    public UserAnalysis queryByUserAndDate(int userId, String summaryDate) {
        return userAnalysisMapper.queryByUserAndDate(userId,summaryDate);
    }

    @Override
    public void update(int userId, String summaryDate, int totalDuration) {
        userAnalysisMapper.update(userId, summaryDate, totalDuration);
    }

    @Override
    public void upsert(int userId, String summaryDate, int totalDuration) {
        UserAnalysis existing = userAnalysisMapper.queryByUserAndDate(userId, summaryDate);
        if (existing == null) {
            UserAnalysis newRecord = new UserAnalysis();
            newRecord.setUserId(userId);
            newRecord.setSummaryDate(summaryDate);
            newRecord.setTotalDuration(totalDuration);
            userAnalysisMapper.insert(newRecord);
        } else {
            userAnalysisMapper.update(userId, summaryDate, totalDuration);
        }
    }
}
