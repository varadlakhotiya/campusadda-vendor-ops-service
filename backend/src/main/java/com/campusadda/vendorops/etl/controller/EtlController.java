package com.campusadda.vendorops.etl.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.etl.dto.request.RunEtlRequest;
import com.campusadda.vendorops.etl.dto.response.EtlJobRunResponse;
import com.campusadda.vendorops.etl.service.EtlOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.campusadda.vendorops.etl.dto.request.BackfillEtlRequest;
import java.time.LocalDate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/etl")
@RequiredArgsConstructor
public class EtlController {

    private final EtlOrchestratorService etlOrchestratorService;

    @PostMapping("/daily-item-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runDailyItemSales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = parseDateTime(request.getWindowStart());
        LocalDateTime end = parseDateTime(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success(
                "Daily item sales ETL completed",
                etlOrchestratorService.runDailyItemSales(start, end)
        ));
    }

    @PostMapping("/daily-vendor-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runDailyVendorSales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = parseDateTime(request.getWindowStart());
        LocalDateTime end = parseDateTime(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success(
                "Daily vendor sales ETL completed",
                etlOrchestratorService.runDailyVendorSales(start, end)
        ));
    }

    @PostMapping("/hourly-sales/run")
    public ResponseEntity<ApiResponse<EtlJobRunResponse>> runHourlySales(@Valid @RequestBody RunEtlRequest request) {
        LocalDateTime start = parseDateTime(request.getWindowStart());
        LocalDateTime end = parseDateTime(request.getWindowEnd());

        return ResponseEntity.ok(ApiResponse.success(
                "Hourly sales ETL completed",
                etlOrchestratorService.runHourlySales(start, end)
        ));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("windowStart/windowEnd must not be blank");
        }
        return LocalDateTime.parse(value);
    }

    @PostMapping("/backfill")

    public ResponseEntity<ApiResponse<String>> backfill(


        @Valid @RequestBody BackfillEtlRequest request

    ) {


    
        LocalDate from = LocalDate.parse(request.getFromDate());

    
        LocalDate to = LocalDate.parse(request.getToDate());

    
        LocalDate lastCompletedDay = LocalDate.now().minusDays(1);

    
        if (from.isAfter(to)) {
    
            throw new IllegalArgumentException(
    
                "fromDate cannot be after toDate"
    
            );
    
        }

    
        if (to.isAfter(lastCompletedDay)) {
    
            throw new IllegalArgumentException(
    
                "Backfill can only run for completed days. Maximum allowed date is "
    
                + lastCompletedDay
    
            );
    
        }

    
        LocalDate current = from;

    
        while (!current.isAfter(to)) {

    
            LocalDateTime start = current.atStartOfDay();


        
            LocalDateTime end = current.plusDays(1).atStartOfDay();

        
            etlOrchestratorService.runDailyItemSales(
        
                start,
        
                end
        
            );

        
            etlOrchestratorService.runDailyVendorSales(
        
                start,
        
                end
        
            );

        
            etlOrchestratorService.runHourlySales(
        
                start,
        
                end
        
            );

        
            current = current.plusDays(1);
    
        }

    
        return ResponseEntity.ok(
    
            ApiResponse.success(
    
                "Backfill completed",
    
                "Processed ETL from "
    
                + request.getFromDate()
    
                + " to "
    
                + request.getToDate()
    
            )
    
        );
    }
    }
