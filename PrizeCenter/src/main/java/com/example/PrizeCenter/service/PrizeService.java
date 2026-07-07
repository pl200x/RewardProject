package com.example.PrizeCenter.service;

import com.example.PrizeCenter.controller.cmd.PrizeCmd;
import com.example.PrizeCenter.entity.Prize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PrizeService {
    Prize queryByCode(String code);
    Prize queryById(int id);
    void addPrize(PrizeCmd prizeCmd);
    void updatePrize(int id,int storage,String name, String description);
    void updatePrizeStorage(String code, int storage,String outBizNo);
    List<Prize> queryAll();
    void deletePrize(int id);
}
