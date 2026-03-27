package com.campusadda.vendorops.etl.scheduler;

import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EtlScheduler {

    private final EtlOrchestratorService etlOrchestratorService;

    @Scheduled(cron = "${app.scheduler.daily-etl-cron:0 5 0 * * *}")
    public void runDailyEtl() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);

        etlOrchestratorService.runDailyItemSales(start, end);
        etlOrchestratorService.runDailyVendorSales(start, end);
        etlOrchestratorService.runHourlySales(start, end);
    }
}