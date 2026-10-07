package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.Repository.PrizeRepository;
import com.example.PrizeCenter.controller.cmd.PrizeCmd;
import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.entity.Prize;
import com.example.PrizeCenter.entity.PrizeRecord;
import com.example.PrizeCenter.exception.PrizeDoNotExistedException;
import com.example.PrizeCenter.exception.PrizeStorageShortageException;
import com.example.PrizeCenter.mapper.PrizeMapper;
import com.example.PrizeCenter.mapper.PrizeRecordMapper;
import com.example.PrizeCenter.service.PrizeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.seata.spring.annotation.GlobalLock;
import com.example.PrizeCenter.service.PrizeService;

import java.util.List;

@Service
public class PrizeServiceImpl implements PrizeService {
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private PrizeRepository prizeRepository;
    @Autowired
    private PrizeRecordMapper prizeRecordMapper;
    @Autowired
    private PrizeRecordService prizeRecordService;
    @Override
    public Prize queryByCode(String code) {
        // Redis is outside Seata AT. Reading stock from MySQL avoids a stale cached
        // after-image surviving a global rollback performed by the resource manager.
        Prize selectedPrize = prizeMapper.queryByCode(code);
        if (selectedPrize == null) {
            throw new PrizeDoNotExistedException("prize code" + code + " do not exist");
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
    @GlobalLock
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
    @GlobalLock
    @Transactional
    public void updatePrizeStorage(String code, int amount,String outbizno) {
        //当amount为0，直接return
        //依赖outbizno做幂等不需要catch duplicateException
        if(amount == 0){
            return;
        }
        // SELECT FOR UPDATE checks the Seata global lock before treating a replay as committed.
        // A normal SELECT/duplicate-key response could observe another transaction's phase-one data.
        Prize prize = prizeMapper.queryByCodeForUpdate(code);
        if(prize == null){
            throw new PrizeDoNotExistedException("this prize is not defined");
        }
        PrizeRecord existing = prizeRecordMapper.queryByOutBizNoForUpdate(outbizno);
        if (existing != null) {
            if (!code.equals(existing.getCode()) || amount != existing.getAmount()) {
                throw new IllegalArgumentException("outbizno already belongs to a different award");
            }
            return;
        }
        // The unique out_biz_no protects replays even after the inventory reaches zero.
        // Both this record and the atomic decrement commit or roll back together.
        PrizeRecordCmd prizeRecordCmd = new PrizeRecordCmd();
        prizeRecordCmd.setCode(code);
        prizeRecordCmd.setAmount(amount);
        prizeRecordCmd.setOutbizno(outbizno);
        prizeRecordService.addPrizeRecord(prizeRecordCmd);
        if (prizeMapper.decrementPrizeStorage(code, amount) != 1) {
            throw new PrizeStorageShortageException("We don't have enough prize now");
        }
        prizeRepository.deletePrize(prize.getCode(),prize.getId());
    }

    @Override
    public List<Prize> queryAll() {
        return prizeMapper.queryAll();
    }

    @Override
    @GlobalLock
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
