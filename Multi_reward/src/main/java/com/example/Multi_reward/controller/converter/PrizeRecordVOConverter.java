package com.example.Multi_reward.controller.converter;

import com.example.Multi_reward.util.DateTimeUtil;
import com.example.Multi_reward.controller.vo.PrizeRecordVO;
import com.example.Multi_reward.entity.PrizeRecord;

import java.util.List;
import java.util.ArrayList;


import static com.example.Multi_reward.constant.Constants.PATTERN;

public class PrizeRecordVOConverter {
    public static PrizeRecordVO convertVO(PrizeRecord record){
        PrizeRecordVO vo = new PrizeRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setBizScene(record.getBizScene());
        vo.setPrizeCode(record.getPrizeCode());
        vo.setPrizeDate(record.getPrizeDate());
        vo.setPrizeStage(record.getPrizeStage());
        vo.setPrizeAmount(record.getPrizeAmount());
        vo.setOutBizNo(record.getOutBizNo());
        vo.setCreateTime(DateTimeUtil.format(record.getCreateTime(),PATTERN));
        vo.setUpdateTime(DateTimeUtil.format(record.getUpdateTime(),PATTERN));
        return vo;
    }
    public static List<PrizeRecordVO> convertVOList(List<PrizeRecord> records){
        List<PrizeRecordVO> voList = new ArrayList<>();
        for(PrizeRecord record : records){
            voList.add(convertVO(record));
        }
        return voList;

    }
}
