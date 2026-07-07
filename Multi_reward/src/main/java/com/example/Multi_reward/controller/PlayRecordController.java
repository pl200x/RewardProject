package com.example.Multi_reward.controller;

import com.example.Multi_reward.controller.converter.PlayRecordVOConverter;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.controller.vo.MultiPlayRecordPageVO;
import com.example.Multi_reward.controller.vo.PlayRecordVO;
import com.example.Multi_reward.controller.vo.SinglePlayRecordPageVO;
import com.example.Multi_reward.entity.PlayRecord;
import com.example.Multi_reward.service.PlayRecordService;
import com.example.Multi_reward.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/playrecord")
public class PlayRecordController {

    private static final Logger log = LoggerFactory.getLogger(PlayRecordController.class);

    @Autowired
    private PlayRecordService playRecordService;

    @GetMapping("/id")
    public SinglePlayRecordPageVO findById(int id) {
        long start = System.currentTimeMillis();
        SinglePlayRecordPageVO vo = new SinglePlayRecordPageVO();
        try {
            PlayRecordVO playRecordVO = PlayRecordVOConverter.convertVO(playRecordService.getId(id));
            vo.setPlayRecordVO(playRecordVO);
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=PLAY_RECORD_QUERY_ERROR id={} error={}", id, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return vo;
    }

    @GetMapping("/today")
    public MultiPlayRecordPageVO findByDate(
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            int userId) {
        long start = System.currentTimeMillis();
        LogContext.setUserId(userId);
        MultiPlayRecordPageVO vo = new MultiPlayRecordPageVO();
        try {
            List<PlayRecord> recordList = playRecordService.getRecordsByDate(startTime, endTime, userId);
            vo.setPlayRecordVOList(PlayRecordVOConverter.convertVOList(recordList));
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=PLAY_RECORD_DATE_QUERY_ERROR userId={} error={}", userId, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return vo;
    }
}