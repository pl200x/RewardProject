package com.example.Multi_reward.util;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.example.Multi_reward.constant.Constants.PRIZE_AMOUNT_RULE_CODE;
import static com.example.Multi_reward.constant.Constants.PRIZE_STAGE_RULE_CODE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)

public class RewardCalculatorTest {
    @Autowired
    private RewardCalculator rewardCalculator;

    @Test
    public void eval() {
        int amount = rewardCalculator.evaluateAmount(PRIZE_AMOUNT_RULE_CODE,1500);
        int stage = rewardCalculator.evaluateStage(PRIZE_STAGE_RULE_CODE,2334);
        System.out.println(amount);
        System.out.println(stage);
    }
}