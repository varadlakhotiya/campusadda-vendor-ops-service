package com.campusadda.vendorops.etl.service;

import com.campusadda.vendorops.etl.dto.response.EtlJobRunResponse;

import java.time.LocalDateTime;

public interface EtlOrchestratorService {
    EtlJobRunResponse runDailyItemSales(LocalDateTime windowStart, LocalDateTime windowEnd);
    EtlJobRunResponse runDailyVendorSales(LocalDateTime windowStart, LocalDateTime windowEnd);
    EtlJobRunResponse runHourlySales(LocalDateTime windowStart, LocalDateTime windowEnd);
}