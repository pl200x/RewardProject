package com.example.Multi_reward.transaction;

import com.example.Multi_reward.MultiRewardApplication;
import com.example.Multi_reward.controller.cmd.PlayCmd;
import com.example.Multi_reward.entity.RewardRetryMessage;
import com.example.Multi_reward.integration.PrizeIntegration;
import com.example.Multi_reward.integration.client.PrizeClient;
import com.example.Multi_reward.service.SyncService;
import com.example.Multi_reward.service.consumer.RetryConsumer;
import com.example.Multi_reward.service.producer.RetryProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.seata.core.context.RootContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real TC, two MySQL schemas, real HTTP/Feign and a separate Prize JVM. No mocked transactions. */
@EnabledIfEnvironmentVariable(named = "RUN_SEATA_TESTS", matches = "true")
class SeataTransactionIT {
    private static Process prize;
    private static String prizeBaseUrl;
    private static ConfigurableApplicationContext context;
    private static JdbcTemplate rewardDb;
    private static JdbcTemplate prizeDb;
    private static FaultInjectingIntegration integration;
    private String code;

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureFixture {
        @Bean @Primary
        FaultInjectingIntegration faultInjectingIntegration(PrizeClient client) {
            return new FaultInjectingIntegration(client);
        }
        @Bean @Primary
        RetryProducer capturingRetryProducer() { return mock(RetryProducer.class); }
    }

    /** Test-only failure AFTER both services have executed their SQL. Never part of a production jar. */
    static class FaultInjectingIntegration extends PrizeIntegration {
        volatile boolean failAfterInsert;
        volatile String lastXid;
        volatile int stockBeforeFailure;
        volatile java.util.concurrent.CountDownLatch paused;
        volatile java.util.concurrent.CountDownLatch resume;
        final java.util.concurrent.atomic.AtomicBoolean holdNext = new java.util.concurrent.atomic.AtomicBoolean();
        FaultInjectingIntegration(PrizeClient client) { super(client); }
        @Override
        public void insertRecord(PlayCmd cmd, String date, int amount, int stage, String key) {
            boolean failThisCall = failAfterInsert;
            if (holdNext.compareAndSet(true, false)) {
                paused.countDown();
                try { assertTrue(resume.await(5, TimeUnit.SECONDS)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
            }
            lastXid = RootContext.getXID();
            assertNotNull(lastXid, "normal and retry flows must enter the global transaction proxy");
            super.insertRecord(cmd, date, amount, stage, key);
            if (failThisCall) {
                stockBeforeFailure = prizeDb.queryForObject("SELECT storage FROM prize WHERE code=?", Integer.class, cmd.getCode());
                throw new IllegalStateException("test failure after both database writes");
            }
        }
    }

    @BeforeAll
    static void start() throws Exception {
        int port;
        try (var socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
        prizeBaseUrl = "http://127.0.0.1:" + port;
        Path root = Path.of("").toAbsolutePath();
        if (!Files.isDirectory(root.resolve("PrizeCenter"))) root = root.getParent();
        Path log = root.resolve("Multi_reward/target/seata-it/prize.log");
        Files.createDirectories(log.getParent());
        prize = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(),
                "-Xmx192m", "-XX:ActiveProcessorCount=2", "-Dlog.path=" + log.getParent().resolve("prize"), "-jar",
                root.resolve("PrizeCenter/target/PrizeCenter-0.0.1-SNAPSHOT.jar").toString(),
                "--server.port=" + port, "--eureka.client.enabled=false", "--seata.enabled=true")
                .directory(root.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
            long deadline = System.nanoTime() + Duration.ofSeconds(50).toNanos();
            boolean ready = false;
            while (System.nanoTime() < deadline && prize.isAlive()) {
                try {
                    ready = http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/actuator/health"))
                            .timeout(Duration.ofSeconds(2)).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
                } catch (Exception ignored) { }
                if (ready) break;
                Thread.sleep(200);
            }
            assertTrue(ready, "Prize must start; inspect " + log);
            context = new SpringApplicationBuilder(MultiRewardApplication.class, FailureFixture.class)
                    .web(WebApplicationType.NONE).registerShutdownHook(false).run(
                            "--spring.profiles.active=direct", "--eureka.client.enabled=false", "--seata.enabled=true",
                            "--integration.prizecenter.base-url=http://127.0.0.1:" + port,
                            "--spring.kafka.listener.auto-startup=false", "--reward.reconciliation.enabled=false");
            rewardDb = context.getBean(JdbcTemplate.class);
            prizeDb = new JdbcTemplate(new DriverManagerDataSource("jdbc:mysql://" + System.getenv("DB_HOST")
                    + ":" + System.getenv("DB_PORT") + "/prize_center", System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD")));
            integration = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(
                    context.getBean(FaultInjectingIntegration.class));
        } catch (Throwable error) { stop(); throw error; }
    }

    @AfterAll
    static void stop() throws Exception {
        if (context != null) context.close();
        if (prize != null) { prize.destroy(); if (!prize.waitFor(12, TimeUnit.SECONDS)) prize.destroyForcibly(); }
    }

    @BeforeEach
    void seed() {
        code = "seata-" + UUID.randomUUID().toString().substring(0, 12);
        prizeDb.update("INSERT INTO prize(code,name,description,storage,create_time,update_time) VALUES(?,?,?,100,NOW(),NOW())", code, code, "Seata test");
        integration.failAfterInsert = false;
        integration.lastXid = null;
        reset(context.getBean(RetryProducer.class));
    }

    @AfterEach
    void cleanup() {
        assertNull(RootContext.getXID(), "global context must not leak between calls");
        rewardDb.update("DELETE FROM prize_record WHERE prize_code=?", code);
        prizeDb.update("DELETE FROM prize_record WHERE code=?", code);
        prizeDb.update("DELETE FROM prize WHERE code=?", code);
        rewardDb.update("DELETE FROM playrecord WHERE user_id=970001");
        rewardDb.update("DELETE FROM user_analysis WHERE user_id=970001");
    }

    private RewardRetryMessage message(String key, int amount) {
        return new RewardRetryMessage(1, amount, LocalDate.now().toString(), code + key,
                new PlayCmd(970001, 1, 0, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "seata-it", code));
    }

    private void assertState(String key, int stock, int records) throws Exception {
        // AT phase two can retry asynchronously, so observe the final committed state with a bounded wait.
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        do {
            if (prizeDb.queryForObject("SELECT storage FROM prize WHERE code=?", Integer.class, code) == stock
                    && prizeDb.queryForObject("SELECT COUNT(*) FROM prize_record WHERE out_biz_no=?", Integer.class, key) == records
                    && rewardDb.queryForObject("SELECT COUNT(*) FROM prize_record WHERE out_biz_no=?", Integer.class, key) == records) return;
            Thread.sleep(100);
        } while (System.nanoTime() < deadline);
        fail("inconsistent distributed award: key=" + key + ", expected stock=" + stock + ", records=" + records);
    }

    @Test
    void commitsBothSchemasAndDuplicateDoesNotSpendInventoryAgain() throws Exception {
        var msg = message("commit", 4);
        context.getBean(SyncService.class).sendPrize(msg);
        assertNotNull(integration.lastXid);
        assertState(msg.getOutbizno(), 96, 1);
        assertThrows(DuplicateKeyException.class, () -> context.getBean(SyncService.class).sendPrize(msg));
        assertState(msg.getOutbizno(), 96, 1);
    }

    @Test
    void rollsBackRemoteStockAndBothRecordsThenKafkaCanRetryWithFreshXid() throws Exception {
        var msg = message("rollback", 4);
        integration.failAfterInsert = true;
        assertThrows(IllegalStateException.class, () -> context.getBean(SyncService.class).sendPrize(msg));
        String failedXid = integration.lastXid;
        assertEquals(96, integration.stockBeforeFailure, "remote branch really executed before failure");
        assertState(msg.getOutbizno(), 100, 0);
        integration.failAfterInsert = false;
        context.getBean(RetryConsumer.class).listen(new ObjectMapper().writeValueAsString(msg));
        assertNotEquals(failedXid, integration.lastXid);
        assertState(msg.getOutbizno(), 96, 1);
        context.getBean(RetryConsumer.class).listen(new ObjectMapper().writeValueAsString(msg));
        assertState(msg.getOutbizno(), 96, 1);
    }

    @Test
    void concurrentReplayCannotTreatAnUncommittedRemoteAwardAsSuccess() throws Exception {
        var msg = message("overlap", 4);
        integration.failAfterInsert = true;
        integration.paused = new java.util.concurrent.CountDownLatch(1);
        integration.resume = new java.util.concurrent.CountDownLatch(1);
        integration.holdNext.set(true);
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> context.getBean(SyncService.class).sendPrize(msg));
            assertTrue(integration.paused.await(5, TimeUnit.SECONDS), "Prize branch must have committed phase one");
            integration.failAfterInsert = false;
            var second = pool.submit(() -> {
                try { context.getBean(SyncService.class).sendPrize(msg); return true; }
                catch (RuntimeException retryable) { return false; }
            });
            Thread.sleep(200);
            assertFalse(second.isDone() && second.get(), "pending global transaction is not a successful replay");
            integration.resume.countDown();
            assertThrows(java.util.concurrent.ExecutionException.class, () -> first.get(10, TimeUnit.SECONDS));
            if (!second.get(10, TimeUnit.SECONDS)) context.getBean(SyncService.class).sendPrize(msg);
            assertState(msg.getOutbizno(), 96, 1);
        } finally {
            integration.resume.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void standaloneLedgerDeleteCannotDestroyAPendingUndoImage() throws Exception {
        var msg = message("protected-delete", 4);
        integration.failAfterInsert = true;
        integration.paused = new java.util.concurrent.CountDownLatch(1);
        integration.resume = new java.util.concurrent.CountDownLatch(1);
        integration.holdNext.set(true);
        var pool = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var first = pool.submit(() -> context.getBean(SyncService.class).sendPrize(msg));
            assertTrue(integration.paused.await(5, TimeUnit.SECONDS));
            int id = prizeDb.queryForObject("SELECT id FROM prize_record WHERE out_biz_no=?", Integer.class, msg.getOutbizno());
            var response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                    URI.create(prizeBaseUrl + "/prize_reward/delete_by_id?id=" + id))
                    .DELETE().timeout(Duration.ofSeconds(4)).build(), HttpResponse.BodyHandlers.ofString());
            assertFalse(new ObjectMapper().readTree(response.body()).path("success").asBoolean());
            assertEquals(1, prizeDb.queryForObject("SELECT COUNT(*) FROM prize_record WHERE id=?", Integer.class, id));
            integration.resume.countDown();
            assertThrows(java.util.concurrent.ExecutionException.class, () -> first.get(10, TimeUnit.SECONDS));
            assertState(msg.getOutbizno(), 100, 0);
        } finally {
            integration.resume.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void businessFailureRollsBackWithoutRewardRecord() throws Exception {
        var msg = message("shortage", 101);
        assertThrows(RuntimeException.class, () -> context.getBean(SyncService.class).sendPrize(msg));
        assertState(msg.getOutbizno(), 100, 0);
    }

    @Test
    void normalSyncUsesProxyAndQueuesOnlyAfterGlobalRollback() throws Exception {
        var msg = message("normal", 1);
        rewardDb.update("INSERT INTO playrecord(user_id,music_id,duration,sync_time,create_time,update_time) VALUES(970001,1,160,DATE_SUB(NOW(),INTERVAL 1 MINUTE),NOW(),NOW())");
        integration.failAfterInsert = true;
        context.getBean(SyncService.class).sync(msg.getPlayCmd());
        var captured = ArgumentCaptor.forClass(RewardRetryMessage.class);
        verify(context.getBean(RetryProducer.class)).send(captured.capture());
        assertNotNull(integration.lastXid, "self-invocation must not bypass Seata");
        assertEquals(99, integration.stockBeforeFailure);
        assertState(captured.getValue().getOutbizno(), 100, 0);
        assertEquals(2, rewardDb.queryForObject("SELECT COUNT(*) FROM playrecord WHERE user_id=970001", Integer.class));
    }
}
