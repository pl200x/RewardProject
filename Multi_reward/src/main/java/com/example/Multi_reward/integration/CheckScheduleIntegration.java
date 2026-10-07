package com.example.Multi_reward.integration;

import com.example.Multi_reward.controller.vo.PrizeRecordCountVO;
import com.example.Multi_reward.controller.vo.PrizeRecordListVO;
import com.example.Multi_reward.integration.client.PrizeClient;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CheckScheduleIntegration {
    private final PrizeClient prizeClient;

    public CheckScheduleIntegration(PrizeClient prizeClient) {
        this.prizeClient = prizeClient;
    }

    public int getPrizeCount() {
        PrizeRecordCountVO result = prizeClient.getPrizeCount();
        if (!result.getBaseVO().isSuccess()) {
            throw new RuntimeException("failed to integrate from PrizeCenter");
        }
        return result.getCount();
    }

    public List<PrizeRecord> getPrizeRecordList() {
        PrizeRecordListVO result = prizeClient.getPrizeRecordList();
        if (!result.getBaseVO().isSuccess()) {
            throw new RuntimeException("failed to integrate from PrizeCenter");
        }
        return result.getPrizeRecordList();
    }
}
