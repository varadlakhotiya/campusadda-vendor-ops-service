package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.etl.dto.response.EtlJobRunResponse;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.repository.EtlJobRunRepository;
import com.campusadda.vendorops.etl.service.DailyItemSalesEtlService;
import com.campusadda.vendorops.etl.service.DailyVendorSalesEtlService;
import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import com.campusadda.vendorops.etl.service.HourlySalesEtlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EtlOrchestratorServiceImpl implements EtlOrchestratorService {

    private final EtlJobRunRepository etlJobRunRepository;
    private final DailyItemSalesEtlService dailyItemSalesEtlService;
    private final DailyVendorSalesEtlService dailyVendorSalesEtlService;
    private final HourlySalesEtlService hourlySalesEtlService;

    @Override
    public EtlJobRunResponse runDailyItemSales(LocalDateTime windowStart, LocalDateTime windowEnd) {
        EtlJobRun run = createRun("DAILY_ITEM_SALES_ETL", "MANUAL", windowStart, windowEnd);
        try {
            int processed = dailyItemSalesEtlService.aggregate(windowStart, windowEnd, run);
            completeRun(run, processed, null);
        } catch (Exception ex) {
            failRun(run, ex.getMessage());
        }
        return map(run);
    }

    @Override
    public EtlJobRunResponse runDailyVendorSales(LocalDateTime windowStart, LocalDateTime windowEnd) {
        EtlJobRun run = createRun("DAILY_VENDOR_SALES_ETL", "MANUAL", windowStart, windowEnd);
        try {
            int processed = dailyVendorSalesEtlService.aggregate(windowStart, windowEnd, run);
            completeRun(run, processed, null);
        } catch (Exception ex) {
            failRun(run, ex.getMessage());
        }
        return map(run);
    }

    @Override
    public EtlJobRunResponse runHourlySales(LocalDateTime windowStart, LocalDateTime windowEnd) {
        EtlJobRun run = createRun("HOURLY_SALES_ETL", "MANUAL", windowStart, windowEnd);
        try {
            int processed = hourlySalesEtlService.aggregate(windowStart, windowEnd, run);
            completeRun(run, processed, null);
        } catch (Exception ex) {
            failRun(run, ex.getMessage());
        }
        return map(run);
    }

    private EtlJobRun createRun(String jobName, String runType, LocalDateTime start, LocalDateTime end) {
        EtlJobRun run = new EtlJobRun();
        run.setJobName(jobName);
        run.setRunType(runType);
        run.setWindowStart(start);
        run.setWindowEnd(end);
        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        return etlJobRunRepository.save(run);
    }

    private void completeRun(EtlJobRun run, int processed, String error) {
        run.setStatus("SUCCESS");
        run.setRecordsProcessed(processed);
        run.setErrorMessage(error);
        run.setFinishedAt(LocalDateTime.now());
        etlJobRunRepository.save(run);
    }

    private void failRun(EtlJobRun run, String error) {
        run.setStatus("FAILED");
        run.setErrorMessage(error);
        run.setFinishedAt(LocalDateTime.now());
        etlJobRunRepository.save(run);
    }

    private EtlJobRunResponse map(EtlJobRun run) {
        return EtlJobRunResponse.builder()
                .id(run.getId())
                .jobName(run.getJobName())
                .runType(run.getRunType())
                .status(run.getStatus())
                .recordsProcessed(run.getRecordsProcessed())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .build();
    }
}