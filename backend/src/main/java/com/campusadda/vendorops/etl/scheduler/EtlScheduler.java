package com.campusadda.vendorops.etl.scheduler;

import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EtlScheduler {

    private final EtlOrchestratorService etlOrchestratorService;

    @Scheduled(cron = "${app.scheduler.daily-etl-cron:0 5 0 * * *}")
    public void runDailyEtl() {

        log.info("========== ETL SCHEDULER STARTED ==========");

        try {

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            LocalDateTime start = yesterday.atStartOfDay();
            LocalDateTime endExclusive = today.atStartOfDay();

            log.info("Running ETL for window {} -> {}", start, endExclusive);

            etlOrchestratorService.runDailyItemSales(start, endExclusive);

            log.info("Daily Item ETL completed");

            etlOrchestratorService.runDailyVendorSales(start, endExclusive);

            log.info("Daily Vendor ETL completed");

            etlOrchestratorService.runHourlySales(start, endExclusive);

            log.info("Hourly Sales ETL completed");

            log.info("========== ETL SCHEDULER FINISHED ==========");

        } catch (Exception ex) {

            log.error("ETL Scheduler failed", ex);
        }
    }
}