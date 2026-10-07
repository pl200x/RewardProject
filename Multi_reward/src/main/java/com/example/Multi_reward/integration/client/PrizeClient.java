package com.example.Multi_reward.integration.client;

import com.example.Multi_reward.controller.cmd.SendRewardCmd;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.controller.vo.PrizeRecordCountVO;
import com.example.Multi_reward.controller.vo.PrizeRecordListVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "prize-service", url = "${integration.prizecenter.base-url:}",
        configuration = InternalFeignConfiguration.class)
public interface PrizeClient {
    @PostMapping(value = "/prize/send_reward", consumes = "application/json")
    BaseVO sendReward(@RequestBody SendRewardCmd command);

    @GetMapping("/prize_reward/count")
    PrizeRecordCountVO getPrizeCount();

    @GetMapping("/prize_reward/record-by-minutes")
    PrizeRecordListVO getPrizeRecordList();
}
