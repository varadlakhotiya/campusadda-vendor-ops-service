package com.campusadda.vendorops.anomaly.controller;

import com.campusadda.vendorops.anomaly.dto.response.AnomalyResponse;
import com.campusadda.vendorops.anomaly.service.AnomalyService;
import com.campusadda.vendorops.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyService anomalyService;

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> scan(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Anomaly scan completed",
                anomalyService.scan(vendorId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> list(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Anomalies fetched successfully",
                anomalyService.getAnomalies(vendorId)));
    }

    @PatchMapping("/{anomalyId}/resolve")
    public ResponseEntity<ApiResponse<AnomalyResponse>> resolve(
            @PathVariable Long vendorId,
            @PathVariable Long anomalyId) {
        return ResponseEntity.ok(ApiResponse.success("Anomaly resolved successfully",
                anomalyService.resolve(vendorId, anomalyId)));
    }
}