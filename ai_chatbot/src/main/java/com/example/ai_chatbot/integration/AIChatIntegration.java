package com.example.ai_chatbot.integration;

import com.example.ai_chatbot.integration.VO.BaseVO;
import com.example.ai_chatbot.integration.VO.ConfigurationVO;
import com.example.ai_chatbot.integration.VO.MultiConfigurationPageVO;
import com.example.ai_chatbot.integration.VO.MultiPlayRecordPageVO;
import com.example.ai_chatbot.integration.VO.MultiPrizeRecordPageVO;
import com.example.ai_chatbot.integration.VO.PlayRecordVO;
import com.example.ai_chatbot.integration.VO.PrizeRecordVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Repository
public class AIChatIntegration {

    private final RestTemplate restTemplate;
    private final String multiRewardBaseUrl;

    public AIChatIntegration(
            RestTemplate restTemplate,
            @Value("${integration.multireward.base-url:http://localhost:8080}")
            String multiRewardBaseUrl) {
        this.restTemplate = restTemplate;
        this.multiRewardBaseUrl = multiRewardBaseUrl;
    }

    public List<PrizeRecordVO> getByUserAndDateRange(
            String startTime,
            String endTime,
            int userId) {
        URI uri = UriComponentsBuilder.fromUriString(multiRewardBaseUrl)
                .path("/prizerecord/by-user-and-date-range")
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("userId", userId)
                .build()
                .encode()
                .toUri();

        MultiPrizeRecordPageVO body = requireSuccessfulBody(
                restTemplate.getForEntity(uri, MultiPrizeRecordPageVO.class),
                "prize records");
        return body.getPrizeRecordVOList();
    }

    public List<PrizeRecordVO> getByUserAndPrizeDate(int userId, String prizeDate) {
        URI uri = UriComponentsBuilder.fromUriString(multiRewardBaseUrl)
                .path("/prizerecord/by-user-and-date")
                .queryParam("userId", userId)
                .queryParam("prizeDate", prizeDate)
                .build()
                .encode()
                .toUri();

        MultiPrizeRecordPageVO body = requireSuccessfulBody(
                restTemplate.getForEntity(uri, MultiPrizeRecordPageVO.class),
                "prize records for " + prizeDate);
        return body.getPrizeRecordVOList();
    }

    public List<PlayRecordVO> findByDate(
            String startTime,
            String endTime,
            int userId) {
        URI uri = UriComponentsBuilder.fromUriString(multiRewardBaseUrl)
                .path("/playrecord/today")
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("userId", userId)
                .build()
                .encode()
                .toUri();

        MultiPlayRecordPageVO body = requireSuccessfulBody(
                restTemplate.getForEntity(uri, MultiPlayRecordPageVO.class),
                "playback records");
        return body.getPlayRecordVOList();
    }

    public List<ConfigurationVO> getAllConfigurations() {
        URI uri = UriComponentsBuilder.fromUriString(multiRewardBaseUrl)
                .path("/config/searchall")
                .build()
                .encode()
                .toUri();

        MultiConfigurationPageVO body = requireSuccessfulBody(
                restTemplate.getForEntity(uri, MultiConfigurationPageVO.class),
                "reward configurations");
        return body.getConfigurationVoList();
    }

    private <T> T requireSuccessfulBody(ResponseEntity<T> response, String resourceName) {
        T body = response.getBody();
        if (body == null) {
            throw new IllegalStateException(
                    "RewardProject returned an empty response while retrieving " + resourceName);
        }

        BaseVO baseVO;
        if (body instanceof MultiPrizeRecordPageVO page) {
            baseVO = page.getBaseVO();
        } else if (body instanceof MultiPlayRecordPageVO page) {
            baseVO = page.getBaseVO();
        } else if (body instanceof MultiConfigurationPageVO page) {
            baseVO = page.getBaseVO();
        } else {
            throw new IllegalStateException("Unsupported RewardProject response type");
        }

        if (baseVO == null || !baseVO.isSuccess()) {
            String detail = baseVO == null ? "missing status" : baseVO.getErrorMessage();
            throw new IllegalStateException(
                    "RewardProject failed while retrieving " + resourceName + ": " + detail);
        }
        return body;
    }
}
