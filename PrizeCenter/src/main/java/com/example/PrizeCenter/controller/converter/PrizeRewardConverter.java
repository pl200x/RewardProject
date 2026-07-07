package com.example.PrizeCenter.controller.converter;

import com.example.PrizeCenter.controller.vo.PrizeRecordVO;
import com.example.PrizeCenter.controller.vo.PrizeVO;
import com.example.PrizeCenter.entity.Prize;
import com.example.PrizeCenter.entity.PrizeRecord;

import java.util.ArrayList;
import java.util.List;

public class PrizeRewardConverter {
    public static PrizeRecordVO convertToVO(PrizeRecord prizeRecord){
        PrizeRecordVO prizeRecordVO = new PrizeRecordVO();
        prizeRecordVO.setId(prizeRecord.getId());
        prizeRecordVO.setAmount(prizeRecord.getAmount());
        prizeRecordVO.setCode(prizeRecord.getCode());
        prizeRecordVO.setOutbizno(prizeRecord.getOutbizno());
        prizeRecordVO.setCreateTime(prizeRecord.getCreateTime());
        prizeRecordVO.setUpdateTime(prizeRecord.getUpdateTime());
        return prizeRecordVO;
    }
    public static List<PrizeRecordVO> convertToVOList(List<PrizeRecord> prizeRecordList){
        List<PrizeRecordVO> prizeRecordVOList = new ArrayList<>();
        for(PrizeRecord prizeRecord : prizeRecordList){
            PrizeRecordVO prizeRecordVO = convertToVO(prizeRecord);
            prizeRecordVOList.add(prizeRecordVO);
        }
        return prizeRecordVOList;
    }
}
