package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.controller.cmd.PrizeRecordCmd;
import com.example.PrizeCenter.entity.PrizeRecord;
import com.example.PrizeCenter.mapper.PrizeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrizeRecordServiceImplTest {

    @Mock
    private PrizeRecordMapper prizeRecordMapper;

    @InjectMocks
    private PrizeRecordServiceImpl prizeRecordService;

    private PrizeRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new PrizeRecord(1, "PRIZE_A", 2, "BIZ_001", new Date(), new Date());
    }

    // ── queryByOutBizNo ──────────────────────────────────────────────────────

    @Test
    void queryByOutBizNo_returnsRecordFromMapper() {
        when(prizeRecordMapper.queryByOutBizNo("BIZ_001")).thenReturn(sampleRecord);

        PrizeRecord result = prizeRecordService.queryByOutBizNo("BIZ_001");

        assertThat(result).isSameAs(sampleRecord);
        verify(prizeRecordMapper).queryByOutBizNo("BIZ_001");
    }

    @Test
    void queryByOutBizNo_returnsNull_whenNotFound() {
        when(prizeRecordMapper.queryByOutBizNo("UNKNOWN")).thenReturn(null);

        assertThat(prizeRecordService.queryByOutBizNo("UNKNOWN")).isNull();
    }

    // ── queryById ────────────────────────────────────────────────────────────

    @Test
    void queryById_returnsRecordFromMapper() {
        when(prizeRecordMapper.queryById(1)).thenReturn(sampleRecord);

        PrizeRecord result = prizeRecordService.queryById(1);

        assertThat(result).isSameAs(sampleRecord);
        verify(prizeRecordMapper).queryById(1);
    }

    @Test
    void queryById_returnsNull_whenNotFound() {
        when(prizeRecordMapper.queryById(999)).thenReturn(null);

        assertThat(prizeRecordService.queryById(999)).isNull();
    }

    // ── addPrizeRecord ───────────────────────────────────────────────────────

    @Test
    void addPrizeRecord_mapsFieldsCorrectlyToEntity() {
        PrizeRecordCmd cmd = new PrizeRecordCmd();
        cmd.setCode("PRIZE_B");
        cmd.setAmount(3);
        cmd.setOutbizno("BIZ_002");

        prizeRecordService.addPrizeRecord(cmd);

        ArgumentCaptor<PrizeRecord> captor = ArgumentCaptor.forClass(PrizeRecord.class);
        verify(prizeRecordMapper).addPrizeRecord(captor.capture());
        PrizeRecord saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("PRIZE_B");
        assertThat(saved.getAmount()).isEqualTo(3);
        assertThat(saved.getOutbizno()).isEqualTo("BIZ_002");
    }

    @Test
    void addPrizeRecord_doesNotCopyTimestampFields() {
        // createTime/updateTime on Cmd should not be propagated — managed by DB
        PrizeRecordCmd cmd = new PrizeRecordCmd();
        cmd.setCode("PRIZE_C");
        cmd.setAmount(1);
        cmd.setOutbizno("BIZ_003");
        cmd.setCreateTime(new Date());
        cmd.setUpdateTime(new Date());

        prizeRecordService.addPrizeRecord(cmd);

        ArgumentCaptor<PrizeRecord> captor = ArgumentCaptor.forClass(PrizeRecord.class);
        verify(prizeRecordMapper).addPrizeRecord(captor.capture());
        assertThat(captor.getValue().getCreateTime()).isNull();
        assertThat(captor.getValue().getUpdateTime()).isNull();
    }

    @Test
    void addPrizeRecord_callsMapperExactlyOnce() {
        PrizeRecordCmd cmd = new PrizeRecordCmd();
        cmd.setCode("PRIZE_D");
        cmd.setAmount(1);
        cmd.setOutbizno("BIZ_004");

        prizeRecordService.addPrizeRecord(cmd);

        verify(prizeRecordMapper, times(1)).addPrizeRecord(any(PrizeRecord.class));
    }

    // ── queryAll ─────────────────────────────────────────────────────────────

    @Test
    void queryAll_returnsListFromMapper() {
        List<PrizeRecord> records = Arrays.asList(sampleRecord);
        when(prizeRecordMapper.queryAll()).thenReturn(records);

        assertThat(prizeRecordService.queryAll()).isSameAs(records);
        verify(prizeRecordMapper).queryAll();
    }

    @Test
    void queryAll_returnsEmptyList_whenNoRecords() {
        when(prizeRecordMapper.queryAll()).thenReturn(List.of());

        assertThat(prizeRecordService.queryAll()).isEmpty();
    }

    // ── deletePrizeRecord ────────────────────────────────────────────────────

    @Test
    void deletePrizeRecord_delegatesToMapper() {
        prizeRecordService.deletePrizeRecord(1);

        verify(prizeRecordMapper).deletePrizeRecord(1);
    }

    // ── getCount ─────────────────────────────────────────────────────────────

    @Test
    void getCount_returnsCountFromMapper() {
        when(prizeRecordMapper.getCount()).thenReturn(42);

        assertThat(prizeRecordService.getCount()).isEqualTo(42);
        verify(prizeRecordMapper).getCount();
    }

    @Test
    void getCount_returnsZero_whenNoRecords() {
        when(prizeRecordMapper.getCount()).thenReturn(0);

        assertThat(prizeRecordService.getCount()).isZero();
    }

    // ── queryLastMinute ──────────────────────────────────────────────────────

    @Test
    void queryLastMinute_delegatesToMapperGetLastMinute() {
        List<PrizeRecord> records = Arrays.asList(sampleRecord);
        when(prizeRecordMapper.getLastMinute()).thenReturn(records);

        assertThat(prizeRecordService.queryLastMinute()).isSameAs(records);
        // 验证调用的是 getLastMinute 而不是其他方法
        verify(prizeRecordMapper).getLastMinute();
        verify(prizeRecordMapper, never()).queryAll();
    }

    @Test
    void queryLastMinute_returnsEmptyList_whenNoneInWindow() {
        when(prizeRecordMapper.getLastMinute()).thenReturn(List.of());

        assertThat(prizeRecordService.queryLastMinute()).isEmpty();
    }
}
