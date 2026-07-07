package com.example.Multi_reward.mapper;

import com.example.Multi_reward.entity.PrizeRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PrizeRecordMapper {
    void insert(PrizeRecord prizeRecord);
    PrizeRecord selectByOutBizNo(String outBizNo);
    List<PrizeRecord> selectByUserAndDate(int userId, String prizeDate);
    int getCount();
    List<PrizeRecord> getLastMinute();
}
