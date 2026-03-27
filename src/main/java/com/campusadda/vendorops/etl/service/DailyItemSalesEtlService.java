package com.campusadda.vendorops.etl.service;

import com.campusadda.vendorops.etl.entity.EtlJobRun;

import java.time.LocalDateTime;

public interface DailyItemSalesEtlService {
    int aggregate(LocalDateTime windowStart, LocalDateTime windowEnd, EtlJobRun etlJobRun);
}