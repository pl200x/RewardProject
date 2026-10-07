package com.example.ai_chatbot.integration;

import com.example.ai_chatbot.integration.VO.BaseVO;
import com.example.ai_chatbot.integration.VO.ConfigurationVO;
import com.example.ai_chatbot.integration.VO.MultiConfigurationPageVO;
import com.example.ai_chatbot.integration.VO.MultiPlayRecordPageVO;
import com.example.ai_chatbot.integration.VO.MultiPrizeRecordPageVO;
import com.example.ai_chatbot.integration.VO.PlayRecordVO;
import com.example.ai_chatbot.integration.VO.PrizeRecordVO;
import org.springframework.stereotype.Repository;
import com.example.ai_chatbot.integration.client.RewardClient;

import java.util.List;

@Repository
public class AIChatIntegration {

    private final RewardClient rewardClient;

    public AIChatIntegration(RewardClient rewardClient) {
        this.rewardClient = rewardClient;
    }

    public List<PrizeRecordVO> getByUserAndDateRange(String startTime, String endTime, int userId) {
        MultiPrizeRecordPageVO body = requireSuccessfulBody(
                rewardClient.getByUserAndDateRange(startTime, endTime, userId), "prize records");
        return body.getPrizeRecordVOList();
    }

    public List<PrizeRecordVO> getByUserAndPrizeDate(int userId, String prizeDate) {
        MultiPrizeRecordPageVO body = requireSuccessfulBody(
                rewardClient.getByUserAndPrizeDate(userId, prizeDate), "prize records for " + prizeDate);
        return body.getPrizeRecordVOList();
    }

    public List<PlayRecordVO> findByDate(String startTime, String endTime, int userId) {
        MultiPlayRecordPageVO body = requireSuccessfulBody(
                rewardClient.findByDate(startTime, endTime, userId), "playback records");
        return body.getPlayRecordVOList();
    }

    public List<ConfigurationVO> getAllConfigurations() {
        MultiConfigurationPageVO body = requireSuccessfulBody(
                rewardClient.getAllConfigurations(), "reward configurations");
        return body.getConfigurationVoList();
    }

    private <T> T requireSuccessfulBody(T body, String resourceName) {
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
