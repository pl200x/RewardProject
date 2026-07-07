package com.example.Multi_reward.controller.converter;

import com.example.Multi_reward.controller.vo.PlayRecordVO;
import com.example.Multi_reward.controller.vo.UserAnalysisVO;
import com.example.Multi_reward.entity.PlayRecord;
import com.example.Multi_reward.entity.UserAnalysis;

import java.util.ArrayList;
import java.util.List;

public class UserAnalysisConverter {
    public static UserAnalysisVO convert(UserAnalysis userAnalysis){
        UserAnalysisVO userAnalysisVO = new UserAnalysisVO();
        userAnalysisVO.setId(userAnalysis.getId());
        userAnalysisVO.setUserId(userAnalysis.getUserId());
        userAnalysisVO.setSummaryDate(userAnalysis.getSummaryDate());
        userAnalysisVO.setCreateTime(userAnalysis.getCreateTime());
        userAnalysisVO.setTotalDuration(userAnalysis.getTotalDuration());
        userAnalysisVO.setUpdateTime(userAnalysis.getUpdateTime());
        userAnalysisVO.setLastPlayTime(userAnalysis.getLastPlayTime());
        return userAnalysisVO;
    }
    public static List<UserAnalysisVO> convertVOList(List<UserAnalysis> analysisList){
        List<UserAnalysisVO> voList = new ArrayList<>();
        for(UserAnalysis analysis : analysisList){
            voList.add(convert(analysis));
        }
        return voList;
    }
}
