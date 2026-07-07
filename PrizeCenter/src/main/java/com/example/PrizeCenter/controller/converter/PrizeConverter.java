package com.example.PrizeCenter.controller.converter;

import com.example.PrizeCenter.controller.vo.PrizeVO;
import com.example.PrizeCenter.entity.Prize;

import java.util.ArrayList;
import java.util.List;

public class PrizeConverter {
    public static PrizeVO convertToVO(Prize prize){
        PrizeVO prizeVO = new PrizeVO();
        prizeVO.setCode(prize.getCode());
        prizeVO.setDescription(prize.getDescription());
        prizeVO.setName(prize.getName());
        prizeVO.setId(prize.getId());
        prizeVO.setStorage(prize.getStorage());
        prizeVO.setCreateTime(prize.getCreateTime());
        prizeVO.setUpdateTime(prize.getUpdateTime());
        return prizeVO;
    }
    public static List<PrizeVO> convertToVOList(List<Prize> prizeList){
        List<PrizeVO> prizeVOList = new ArrayList<>();
        for(Prize prize : prizeList){
            PrizeVO prizeVO = convertToVO(prize);
            prizeVOList.add(prizeVO);
        }
        return prizeVOList;
    }
}
