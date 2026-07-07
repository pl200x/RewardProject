package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.Repository.PrizeRepository;
import com.example.PrizeCenter.controller.cmd.PrizeCmd;
import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.entity.Prize;
import com.example.PrizeCenter.entity.PrizeRecord;
import com.example.PrizeCenter.exception.PrizeDoNotExistedException;
import com.example.PrizeCenter.exception.PrizeStorageShortageException;
import com.example.PrizeCenter.mapper.PrizeMapper;
import com.example.PrizeCenter.service.PrizeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.PrizeCenter.service.PrizeService;

import java.util.List;

@Service
public class PrizeServiceImpl implements PrizeService {
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private PrizeRepository prizeRepository;
    @Autowired
    private PrizeRecordService prizeRecordService;
    @Override
    public Prize queryByCode(String code) {
        Prize selectedPrize = prizeRepository.getByCode(code);
        if(selectedPrize == null){
            selectedPrize = prizeMapper.queryByCode(code);
            if(selectedPrize == null) {
                throw new PrizeDoNotExistedException("prize code" + code + " do not exist");
            }
            prizeRepository.addPrizeByCode(selectedPrize);
        }
        return selectedPrize;
    }

    @Override
    public Prize queryById(int id) {
        return prizeMapper.queryById(id);
    }

    @Override
    @Transactional
    public void addPrize(PrizeCmd prizeCmd) {
        Prize prize = new Prize();
        prize.setCode(prizeCmd.getCode());
        prize.setDescription(prizeCmd.getDescription());
        prize.setName(prizeCmd.getName());
        prize.setStorage(prizeCmd.getStorage());
        //缓存内存有限，新建prize只会添加到数据库
        prizeMapper.addPrize(prize);
    }

    @Override
    @Transactional
    public void updatePrize(int id, int storage, String name, String description) {
        Prize prize = queryById(id);
        if(prize == null){
            throw new PrizeDoNotExistedException("this prize is not defined");
        }
        prizeMapper.updatePrize(id,storage,name,description);
        prizeRepository.deletePrize(prize.getCode(),id);
    }

    @Override
    @Transactional
    public void updatePrizeStorage(String code, int amount,String outbizno) {
        //当amount为0，直接return
        //依赖outbizno做幂等不需要catch duplicateException
        if(amount == 0){
            return;
        }
        Prize prize = queryByCode(code);
        if(prize == null){
            throw new PrizeDoNotExistedException("this prize is not defined");
        }
        int totalStorage = prize.getStorage();
        int newStorage = totalStorage - amount;
        if(totalStorage < amount ){
            throw new PrizeStorageShortageException("We don't have enough prize now");
        }
        prizeMapper.updatePrizeStorage(code,newStorage);
        prizeRepository.deletePrize(prize.getCode(),prize.getId());
        PrizeRecordCmd prizeRecordCmd = new PrizeRecordCmd();
        prizeRecordCmd.setCode(code);
        prizeRecordCmd.setAmount(amount);
        prizeRecordCmd.setOutbizno(outbizno);
        prizeRecordService.addPrizeRecord(prizeRecordCmd);
    }

    @Override
    public List<Prize> queryAll() {
        return prizeMapper.queryAll();
    }

    @Override
    @Transactional
    public void deletePrize(int id) {
        Prize prize = queryById(id);
        if(prize == null){
            throw new PrizeDoNotExistedException("this prize is not defined");
        }
        prizeMapper.deletePrize(id);
        prizeRepository.deletePrize(prize.getCode(),id);
    }
}
