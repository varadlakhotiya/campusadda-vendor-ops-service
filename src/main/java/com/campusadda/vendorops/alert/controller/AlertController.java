package com.campusadda.vendorops.alert.controller;

import com.campusadda.vendorops.alert.dto.response.AlertResponse;
import com.campusadda.vendorops.alert.service.AlertService;
import com.campusadda.vendorops.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlerts(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Alerts fetched successfully",
                alertService.getAlerts(vendorId)));
    }

    @PatchMapping("/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledge(
            @PathVariable Long vendorId,
            @PathVariable Long alertId) {
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged successfully",
                alertService.acknowledgeAlert(vendorId, alertId)));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>> resolve(
            @PathVariable Long vendorId,
            @PathVariable Long alertId) {
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully",
                alertService.resolveAlert(vendorId, alertId)));
    }
}