package com.campusadda.vendorops.etl.scheduler;

import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.repository.EtlJobRunRepository;
import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EtlScheduler {

    private static final String DAILY_VENDOR_JOB =
            "DAILY_VENDOR_SALES_ETL";

    private final EtlOrchestratorService etlOrchestratorService;
    private final EtlJobRunRepository etlJobRunRepository;

    @Scheduled(cron = "0 5 0 * * *")
    public void runNightlyAggregations() {

        try {

            LocalDate startDate = determineBackfillStartDate();
            LocalDate today = LocalDate.now();

            while (startDate.isBefore(today)) {

                LocalDateTime windowStart =
                        startDate.atStartOfDay();

                LocalDateTime windowEnd =
                        startDate.plusDays(1).atStartOfDay();

                log.info(
                        "Running ETL for window {} -> {}",
                        windowStart,
                        windowEnd
                );

                etlOrchestratorService.runDailyItemSales(
                        windowStart,
                        windowEnd
                );

                etlOrchestratorService.runDailyVendorSales(
                        windowStart,
                        windowEnd
                );

                etlOrchestratorService.runHourlySales(
                        windowStart,
                        windowEnd
                );

                startDate = startDate.plusDays(1);
            }

        } catch (Exception ex) {

            log.error(
                    "Nightly ETL execution failed",
                    ex
            );
        }
    }

    private LocalDate determineBackfillStartDate() {

        Optional<EtlJobRun> latestRun =
                etlJobRunRepository
                        .findTopByJobNameAndStatusOrderByWindowEndDesc(
                                DAILY_VENDOR_JOB,
                                "SUCCESS"
                        );

        if (latestRun.isPresent()) {

            return latestRun.get()
                    .getWindowEnd()
                    .toLocalDate();
        }

        

        return LocalDate.now().minusDays(1);
    }
}