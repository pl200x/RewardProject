package com.example.Multi_reward.controller;

import com.example.Multi_reward.controller.cmd.ConfigurationCmd;
import com.example.Multi_reward.controller.converter.ConfigurationConverter;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.controller.vo.ConfigurationVO;
import com.example.Multi_reward.controller.vo.MultiConfigurationPageVO;
import com.example.Multi_reward.controller.vo.SingleConfigurationPageVO;
import com.example.Multi_reward.entity.Configuration;
import com.example.Multi_reward.exception.ConfigurationNotExistException;
import com.example.Multi_reward.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationController.class);

    @Autowired
    private ConfigurationService configurationService;

    @PostMapping("/add")
    public BaseVO addConfiguration(@RequestBody ConfigurationCmd cmd) {
        long start = System.currentTimeMillis();
        try {
            configurationService.add(cmd);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (Exception e) {
            log.error("event=CONFIG_ADD_ERROR code={} error={}", cmd.getCode(), e.getMessage(), e);
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }

    @PutMapping("/update")
    public BaseVO updateConfiguration(String code, String rule, String description) {
        long start = System.currentTimeMillis();
        try {
            configurationService.update(code, rule, description);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (ConfigurationNotExistException e) {
            log.error("event=CONFIG_UPDATE_ERROR code={} error={}", code, e.getMessage(), e);
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CONFIG_UPDATE_ERROR code={} error={}", code, e.getMessage(), e);
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }

    @DeleteMapping("/delete")
    public BaseVO deleteConfiguration(String code) {
        long start = System.currentTimeMillis();
        try {
            configurationService.delete(code);
            return BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null);
        } catch (Exception e) {
            log.error("event=CONFIG_DELETE_ERROR code={} error={}", code, e.getMessage(), e);
            return BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, "unknown error");
        }
    }

    @GetMapping("/search")
    public SingleConfigurationPageVO searchSingleConfiguration(String code) {
        long start = System.currentTimeMillis();
        SingleConfigurationPageVO vo = new SingleConfigurationPageVO();
        try {
            Configuration configuration = configurationService.queryByCode(code);
            vo.setConfigurationVO(ConfigurationConverter.convertToVO(configuration));
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=CONFIG_SEARCH_ERROR code={} error={}", code, e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, null));
        }
        return vo;
    }

    @GetMapping("/searchall")
    public MultiConfigurationPageVO searchMultiConfiguration() {
        long start = System.currentTimeMillis();
        MultiConfigurationPageVO vo = new MultiConfigurationPageVO();
        try {
            List<Configuration> configurationList = configurationService.queryAll();
            vo.setConfigurationVoList(ConfigurationConverter.convertToVoList(configurationList));
            vo.setBaseVO(BaseVO.buildBaseVO(true, 200, System.currentTimeMillis() - start, null));
        } catch (Exception e) {
            log.error("event=CONFIG_SEARCH_ALL_ERROR error={}", e.getMessage(), e);
            vo.setBaseVO(BaseVO.buildBaseVO(false, 500, System.currentTimeMillis() - start, null));
        }
        return vo;
    }
}