package com.example.Multi_reward.service;

import com.example.Multi_reward.entity.UserAnalysis;

public interface UserAnalysisService {
    void insert(UserAnalysis userAnalysis);
    UserAnalysis queryByUserAndDate(int userId, String summaryDate);
    void update(int userId, String summaryDate, int totalDuration);
    void upsert(int userId, String summaryDate, int totalDuration);
}
