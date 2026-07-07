package com.example.Multi_reward.controller.converter;

import com.example.Multi_reward.util.DateTimeUtil;
import com.example.Multi_reward.controller.vo.PlayRecordVO;
import com.example.Multi_reward.entity.PlayRecord;

import java.util.ArrayList;
import java.util.List;

import static com.example.Multi_reward.constant.Constants.PATTERN;

public class PlayRecordVOConverter {
    public static PlayRecordVO convertVO(PlayRecord record){
        PlayRecordVO vo = new PlayRecordVO();
        vo.setId(record.getId());
        vo.setDuration(record.getDuration());
        vo.setCreateTime(DateTimeUtil.format(record.getCreateTime(),PATTERN));
        vo.setUpdateTime(DateTimeUtil.format(record.getUpdateTime(),PATTERN));
        vo.setSyncTime(DateTimeUtil.format(record.getSyncTime(),PATTERN));
        vo.setMusicId(record.getMusicId());
        vo.setUserId(record.getUserId());
        return vo;
    }
    public static List<PlayRecordVO> convertVOList(List<PlayRecord> records){
        List<PlayRecordVO> voList = new ArrayList<>();
        for(PlayRecord record : records){
            voList.add(convertVO(record));
        }
        return voList;
    }
}
