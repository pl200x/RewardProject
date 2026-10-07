package com.example.ai_chatbot.integration.client;

import com.example.ai_chatbot.integration.VO.MultiConfigurationPageVO;
import com.example.ai_chatbot.integration.VO.MultiPlayRecordPageVO;
import com.example.ai_chatbot.integration.VO.MultiPrizeRecordPageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reward-service", url = "${integration.multireward.base-url:}",
        configuration = InternalFeignConfiguration.class)
public interface RewardClient {
    @GetMapping("/prizerecord/by-user-and-date-range")
    MultiPrizeRecordPageVO getByUserAndDateRange(@RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime, @RequestParam("userId") int userId);

    @GetMapping("/prizerecord/by-user-and-date")
    MultiPrizeRecordPageVO getByUserAndPrizeDate(@RequestParam("userId") int userId,
            @RequestParam("prizeDate") String prizeDate);

    @GetMapping("/playrecord/today")
    MultiPlayRecordPageVO findByDate(@RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime, @RequestParam("userId") int userId);

    @GetMapping("/config/searchall")
    MultiConfigurationPageVO getAllConfigurations();
}
