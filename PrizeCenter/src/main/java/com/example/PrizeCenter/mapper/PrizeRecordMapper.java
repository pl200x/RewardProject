package com.example.PrizeCenter.mapper;

import com.example.PrizeCenter.entity.PrizeRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PrizeRecordMapper {
    PrizeRecord queryByOutBizNo(String outbizno);
    PrizeRecord queryById(int id);
    void addPrizeRecord(PrizeRecord prizeRecord);
    List<PrizeRecord> queryAll();
    void deletePrizeRecord(int id);
    int getCount();
    List<PrizeRecord> getLastMinute();
}
