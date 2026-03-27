package com.campusadda.vendorops.etl.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.etl.dto.request.RunEtlRequest;
import com.campusadda.vendorops.etl.dto.response.EtlJobRunResponse;
import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/etl")
@RequiredArgsConstructor
public class EtlController {

    private final EtlOrchestratorService etlOrchestratorService;

    @PostMapping("/daily-item-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runDailyItemSales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = LocalDateTime.parse(request.getWindowStart());
        LocalDateTime end = LocalDateTime.parse(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success("Daily item sales ETL completed",
                etlOrchestratorService.runDailyItemSales(start, end)));
    }

    @PostMapping("/daily-vendor-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runDailyVendorSales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = LocalDateTime.parse(request.getWindowStart());
        LocalDateTime end = LocalDateTime.parse(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success("Daily vendor sales ETL completed",
                etlOrchestratorService.runDailyVendorSales(start, end)));
    }

    @PostMapping("/hourly-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runHourlySales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = LocalDateTime.parse(request.getWindowStart());
        LocalDateTime end = LocalDateTime.parse(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success("Hourly sales ETL completed",
                etlOrchestratorService.runHourlySales(start, end)));
    }
}