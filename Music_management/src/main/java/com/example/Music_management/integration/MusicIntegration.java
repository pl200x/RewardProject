package com.example.Music_management.integration;


import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.service.MusicService;
import com.example.Music_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Repository
public class MusicIntegration {
    @Autowired
    private RestTemplate restTemplate;

    /** Multi_reward 地址(播放上报;部署时外部化覆盖,同 PrizeIntegration 模式) */
    @Value("${integration.multireward.base-url:http://localhost:8080}")
    private String multiRewardBaseUrl;

    public void syncMusicPlayRecord(PlayRecordCmd playRecordCmd){
        String url = multiRewardBaseUrl + "/sync";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PlayRecordCmd> httpEntity = new HttpEntity<>(playRecordCmd, headers);
        ResponseEntity<BaseVO> result = restTemplate.postForEntity(url, httpEntity, BaseVO.class);
        if(!result.getBody().isSuccess()){
            throw new RuntimeException("failed to integrate MultiReward");
        }
    }
}
