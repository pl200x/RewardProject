package com.example.Multi_reward.mapper;

import com.example.Multi_reward.entity.UserAnalysis;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAnalysisMapper {
    void insert(UserAnalysis analysis);
    UserAnalysis queryByUserAndDate(int userId,String summaryDate);
    void update(int userId, String summaryDate, int totalDuration);

}
