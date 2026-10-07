package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.Repository.PrizeRepository;
import com.example.PrizeCenter.controller.PrizeController;
import com.example.PrizeCenter.controller.cmd.SendRewardCmd;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Enable only against disposable test infrastructure. Uses real MySQL transactions/unique keys. */
@SpringBootTest(properties = {"eureka.client.enabled=false", "seata.enabled=false"})
@EnabledIfEnvironmentVariable(named = "RUN_DATABASE_TESTS", matches = "true")
class PrizeStockConcurrencyTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PrizeController controller;
    @Autowired private PrizeRepository cache;
    private String code;
    private int id;

    @BeforeEach
    void createInventory() {
        code = "test-" + UUID.randomUUID().toString().substring(0, 16);
        jdbc.update("INSERT INTO prize(code,name,description,storage,create_time,update_time) VALUES(?,?,?,100,NOW(),NOW())",
                code, "test", "isolated concurrency test");
        id = jdbc.queryForObject("SELECT id FROM prize WHERE code=?", Integer.class, code);
    }

    @AfterEach
    void removeOnlyTestRows() {
        jdbc.update("DELETE FROM prize_record WHERE code=?", code);
        jdbc.update("DELETE FROM prize WHERE code=?", code);
        cache.deletePrize(code, id);
    }

    @Test
    void differentConcurrentAwardsCannotLoseInventoryDecrements() throws Exception {
        var executor = Executors.newFixedThreadPool(8);
        try {
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 32; i++) {
                String key = code + "-" + i;
                results.add(executor.submit(() -> controller.sendReward(command(key, 1)).isSuccess()));
            }
            for (Future<Boolean> result : results) assertTrue(result.get(10, TimeUnit.SECONDS));
            assertEquals(68, jdbc.queryForObject("SELECT storage FROM prize WHERE code=?", Integer.class, code));
            assertEquals(32, jdbc.queryForObject("SELECT COUNT(*) FROM prize_record WHERE code=?", Integer.class, code));
        } finally { executor.shutdownNow(); }
    }

    @Test
    void replayStillSucceedsAfterInventoryIsExhausted() {
        SendRewardCmd command = command(code + "-replay", 100);
        assertTrue(controller.sendReward(command).isSuccess());
        assertTrue(controller.sendReward(command).isSuccess());
        assertEquals(0, jdbc.queryForObject("SELECT storage FROM prize WHERE code=?", Integer.class, code));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM prize_record WHERE code=?", Integer.class, code));
    }

    @Test
    void failedDecrementRollsBackItsIdempotencyRecord() {
        assertFalse(controller.sendReward(command(code + "-shortage", 101)).isSuccess());
        assertEquals(100, jdbc.queryForObject("SELECT storage FROM prize WHERE code=?", Integer.class, code));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM prize_record WHERE code=?", Integer.class, code));
    }

    private SendRewardCmd command(String key, int amount) {
        SendRewardCmd command = new SendRewardCmd();
        command.setCode(code);
        command.setAmount(amount);
        command.setOutbizno(key);
        return command;
    }
}
