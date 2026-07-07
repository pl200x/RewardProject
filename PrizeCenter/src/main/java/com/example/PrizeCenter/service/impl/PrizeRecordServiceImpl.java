package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.entity.PrizeRecord;
import com.example.PrizeCenter.mapper.PrizeRecordMapper;
import com.example.PrizeCenter.service.PrizeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeRecordServiceImpl implements PrizeRecordService {
    @Autowired
    private PrizeRecordMapper prizeRecordMapper;
    @Override
    public PrizeRecord queryByOutBizNo(String outbizno) {
        return prizeRecordMapper.queryByOutBizNo(outbizno);
    }

    @Override
    public PrizeRecord queryById(int id) {
        return prizeRecordMapper.queryById(id);
    }

    @Override
    public void addPrizeRecord(PrizeRecordCmd prizeRecordCmd) {
        PrizeRecord prizeRecord = new PrizeRecord();
        prizeRecord.setCode(prizeRecordCmd.getCode());
        prizeRecord.setAmount(prizeRecordCmd.getAmount());
        prizeRecord.setOutbizno(prizeRecordCmd.getOutbizno());
        prizeRecordMapper.addPrizeRecord(prizeRecord);
    }

    @Override
    public List<PrizeRecord> queryAll() {

        return prizeRecordMapper.queryAll();
    }

    @Override
    public void deletePrizeRecord(int id) {
        prizeRecordMapper.deletePrizeRecord(id);
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
