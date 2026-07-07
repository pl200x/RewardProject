package com.example.PrizeCenter.service;

import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.entity.PrizeRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PrizeRecordService {
    PrizeRecord queryByOutBizNo(String outbizno);
    PrizeRecord queryById(int id);
    void addPrizeRecord(PrizeRecordCmd prizeRecordCmd);
    List<PrizeRecord> queryAll();
    void deletePrizeRecord(int id);
    int getCount();
    List<PrizeRecord> queryLastMinute();
}
