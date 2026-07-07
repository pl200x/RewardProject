package com.example.Multi_reward.controller;

import com.example.Multi_reward.controller.cmd.PlayCmd;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.service.SyncService;
import com.example.Multi_reward.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class SyncTimeController {

    @Autowired
    private SyncService syncService;

    private static final Logger log = LoggerFactory.getLogger(SyncTimeController.class);

    @PostMapping
    public BaseVO sync(@RequestBody PlayCmd playCmd) {
        long start = System.currentTimeMillis();
        LogContext.setUserId(playCmd.getUserId());
        try {
            syncService.sync(playCmd);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (Exception e) {
            log.error("event=SYNC_ERROR userId={} error={}", playCmd.getUserId(), e.getMessage(), e);
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }
}