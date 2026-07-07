package com.example.Multi_reward.integration;

import com.example.Multi_reward.controller.vo.PrizeRecordCountVO;
import com.example.Multi_reward.controller.vo.PrizeRecordListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
public class CheckScheduleIntegration {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${integration.prizecenter.base-url:http://localhost:8082}")
    private String prizeCenterBaseUrl;
    public int getPrizeCount(){
        String url =  prizeCenterBaseUrl + "/prize_reward/count";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<PrizeRecordCountVO> result = restTemplate.getForEntity(url,PrizeRecordCountVO.class);
        if(!result.getBody().getBaseVO().isSuccess()){
            throw new RuntimeException("failed to integrate from PrizeCenter");
        }
        return result.getBody().getCount();
    }
    public List<PrizeRecord> getPrizeRecordList(){
        String url =  prizeCenterBaseUrl + "/prize_reward/record-by-minutes";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<PrizeRecordListVO> result = restTemplate.getForEntity(url,PrizeRecordListVO.class);
        if(!result.getBody().getBaseVO().isSuccess()){
            throw new RuntimeException("failed to integrate from PrizeCenter");
        }
        return result.getBody().getPrizeRecordList();
    }
}
