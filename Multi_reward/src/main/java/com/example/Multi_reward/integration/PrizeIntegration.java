package com.example.Multi_reward.integration;


import com.example.Multi_reward.controller.cmd.PlayCmd;
import com.example.Multi_reward.controller.cmd.SendRewardCmd;
import com.example.Multi_reward.controller.vo.BaseVO;
import com.example.Multi_reward.entity.PrizeRecord;
import com.example.Multi_reward.service.PrizeRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

/**
 * 发奖集成:调 PrizeCenter 扣库存(/prize/send_reward 按 cmd.code 区分 coin/gem/coupon)+ 落本地发奖流水。
 * PrizeCenter 地址可配置(integration.prizecenter.base-url),部署时外部化,本地默认 8082。
 */
@Repository
public class PrizeIntegration {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PrizeRecordService prizeRecordService;
    private static final Logger log = LoggerFactory.getLogger(PrizeIntegration.class);

    @Value("${integration.prizecenter.base-url:http://localhost:8082}")
    private String prizeCenterBaseUrl;

    public void sendPrize(PlayCmd playCmd, int stage, int amount, String today, String outbizno) {
        String code = playCmd.getCode();
        int userId = playCmd.getUserId();
        send(code, amount, outbizno);
        insertRecord(playCmd, today, amount, stage, outbizno);
        log.info("event=PRIZE_SENT userId={} code={} amount={} stage={} date={} outbizno={}",
                userId, code, amount, stage, today, outbizno);
    }

    //TODO:CMD的日期转化问题
    public void send(String code, int amount, String outbizno) {
        SendRewardCmd rewardCmd = new SendRewardCmd();
        rewardCmd.setCode(code);
        rewardCmd.setAmount(amount);
        rewardCmd.setOutbizno(outbizno);
        String url = prizeCenterBaseUrl + "/prize/send_reward";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SendRewardCmd> httpEntity = new HttpEntity<>(rewardCmd, headers);
        ResponseEntity<BaseVO> result = restTemplate.postForEntity(url, httpEntity, BaseVO.class);
        if(!result.getBody().isSuccess()){
            // 透传 PrizeCenter 的具体失败原因(奖品不存在/库存不足等),便于重试队列日志排查
            throw new RuntimeException("PrizeCenter send_reward failed: " + result.getBody().getErrorMessage());
        }
    }

    public void insertRecord(PlayCmd playCmd, String today, int amount, int stage, String outbizno) {
        PrizeRecord prizeRecord = new PrizeRecord();
        prizeRecord.setBizScene(playCmd.getScene());
        prizeRecord.setUserId(playCmd.getUserId());
        prizeRecord.setPrizeDate(today);
        prizeRecord.setPrizeAmount(amount);
        prizeRecord.setPrizeStage(stage);
        prizeRecord.setPrizeCode(playCmd.getCode());
        prizeRecord.setOutBizNo(outbizno);
        prizeRecordService.insert(prizeRecord);
    }
}
