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
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Use an exclusive end boundary to avoid missing records in the last second.
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime endExclusive = today.atStartOfDay();

        etlOrchestratorService.runDailyItemSales(start, endExclusive);
        etlOrchestratorService.runDailyVendorSales(start, endExclusive);
        etlOrchestratorService.runHourlySales(start, endExclusive);
    }
}
