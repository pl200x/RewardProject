package com.example.Music_management.integration;

import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.integration.client.RewardClient;
import org.springframework.stereotype.Repository;

@Repository
public class MusicIntegration {
    private final RewardClient rewardClient;

    public MusicIntegration(RewardClient rewardClient) {
        this.rewardClient = rewardClient;
    }

    public void syncMusicPlayRecord(PlayRecordCmd playRecordCmd) {
        BaseVO result = rewardClient.syncMusicPlayRecord(playRecordCmd);
        if (!result.isSuccess()) {
            throw new RuntimeException("failed to integrate MultiReward");
        }
    }
}
