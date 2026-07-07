package com.example.Multi_reward.controller;

import com.example.Multi_reward.controller.converter.UserAnalysisConverter;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.controller.vo.UserAnalysisPageVO;
import com.example.Multi_reward.controller.vo.UserAnalysisVO;
import com.example.Multi_reward.entity.UserAnalysis;
import com.example.Multi_reward.service.UserAnalysisService;
import com.example.Multi_reward.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
public class UserAnalysisController {

    @Autowired
    private UserAnalysisService userAnalysisService;

    private static final Logger log = LoggerFactory.getLogger(UserAnalysisController.class);

    @GetMapping("/id")
    public UserAnalysisPageVO getUserAnalysis(int userId, String summaryDate) {
        long start = System.currentTimeMillis();
        LogContext.setUserId(userId);
        UserAnalysisPageVO vo = new UserAnalysisPageVO();
        try {
            UserAnalysis analysis = userAnalysisService.queryByUserAndDate(userId, summaryDate);
            // 之前漏了 setUserAnalysisVO,导致永远返回 null;且当天无记录(analysis==null)时 convert 会 NPE→500。
            if (analysis != null) {
                vo.setUserAnalysisVO(UserAnalysisConverter.convert(analysis));
            }
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
            return vo;
        } catch (Exception e) {
            log.error("event=USER_ANALYSIS_QUERY_ERROR userId={} summaryDate={} error={}",
                    userId, summaryDate, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error"));
            return vo;
        }
    }
}