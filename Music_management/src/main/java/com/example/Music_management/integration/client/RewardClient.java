package com.example.Music_management.integration.client;

import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.integration.PlayRecordCmd;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reward-service", url = "${integration.multireward.base-url:}",
        configuration = InternalFeignConfiguration.class)
public interface RewardClient {
    @PostMapping(value = "/sync", consumes = "application/json")
    BaseVO syncMusicPlayRecord(@RequestBody PlayRecordCmd command);
}
