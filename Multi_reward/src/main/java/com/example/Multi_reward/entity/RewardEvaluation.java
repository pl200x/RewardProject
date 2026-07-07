package com.example.Multi_reward.entity;

public class RewardEvaluation {
    private final int stage;
    private final int amount;

    public RewardEvaluation(int stage, int amount) {
        this.stage = stage;
        this.amount = amount;
    }

    public boolean hasReward() {
        return stage > 0 && amount > 0;
    }

    public int getStage() {
        return stage;
    }

    public int getAmount() {
        return amount;
    }
}