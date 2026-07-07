package com.example.Multi_reward.controller;

import com.example.Multi_reward.controller.converter.PrizeRecordVOConverter;
import com.example.Multi_reward.controller.vo.*;
import com.example.Multi_reward.entity.PrizeRecord;
import com.example.Multi_reward.service.CheckScheduleService;
import com.example.Multi_reward.service.PrizeRecordService;
import com.example.Multi_reward.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prizerecord")
public class PrizeRecordController {

    @Autowired
    private PrizeRecordService prizeRecordService;
    @Autowired
    private CheckScheduleService checkScheduleService;

    private static final Logger log = LoggerFactory.getLogger(PrizeRecordController.class);

    @GetMapping("/by-outbizno")
    public SinglePrizeRecordPageVO getByOutBizNo(String outBizNo) {
        long start = System.currentTimeMillis();
        SinglePrizeRecordPageVO vo = new SinglePrizeRecordPageVO();
        try {
            PrizeRecord record = prizeRecordService.selectByOutBizNo(outBizNo);
            vo.setPrizeRecordVO(PrizeRecordVOConverter.convertVO(record));
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=PRIZE_RECORD_QUERY_ERROR outBizNo={} error={}", outBizNo, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return vo;
    }

    @GetMapping("/by-user-and-date")
    public MultiPrizeRecordPageVO getByUserAndDate(int userId, String prizeDate) {
        long start = System.currentTimeMillis();
        LogContext.setUserId(userId);
        MultiPrizeRecordPageVO vo = new MultiPrizeRecordPageVO();
        try {
            List<PrizeRecord> recordList = prizeRecordService.selectByUserAndDate(userId, prizeDate);
            vo.setPrizeRecordVOList(PrizeRecordVOConverter.convertVOList(recordList));
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=PRIZE_RECORD_DATE_QUERY_ERROR userId={} prizeDate={} error={}",
                    userId, prizeDate, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
        }
        return vo;
    }
}