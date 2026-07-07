package com.example.Multi_reward.service;

import com.example.Multi_reward.controller.cmd.PlayCmd;
import com.example.Multi_reward.entity.RewardRetryMessage;

public interface SyncService {
    void sync(PlayCmd playCmd);
    void sendPrize(RewardRetryMessage rewardRetryMessage);
}
