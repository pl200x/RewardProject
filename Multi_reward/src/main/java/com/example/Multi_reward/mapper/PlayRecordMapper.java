package com.example.Multi_reward.mapper;

import com.example.Multi_reward.entity.PlayRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface PlayRecordMapper {
    PlayRecord queryById(int id);
    List<PlayRecord> queryByDate(Date startTime, Date endTime, int userId);
    void addRecord(PlayRecord record);


}
