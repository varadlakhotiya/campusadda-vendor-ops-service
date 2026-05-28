package com.campusadda.vendorops.vendor.controller;

import java.util.List;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.vendor.dto.request.CreateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorStatusRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorDetailResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorSummaryResponse;
import com.campusadda.vendorops.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorResponse>> createVendor(
            @Valid @RequestBody CreateVendorRequest request) {

        VendorResponse response = vendorService.createVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getAllVendors() {
        List<VendorResponse> response = vendorService.getAllVendors();
        return ResponseEntity.ok(ApiResponse.success("Vendors fetched successfully", response));
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorDetailResponse>> getVendorById(
            @PathVariable Long vendorId) {

        VendorDetailResponse response = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor fetched successfully", response));
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody UpdateVendorRequest request) {

        VendorResponse response = vendorService.updateVendor(vendorId, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", response));
    }

    @PatchMapping("/{vendorId}/status")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendorStatus(
            @PathVariable Long vendorId,
            @Valid @RequestBody UpdateVendorStatusRequest request) {

        VendorResponse response = vendorService.updateVendorStatus(vendorId, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor status updated successfully", response));
    }

    @GetMapping("/{vendorId}/summary")
    public ResponseEntity<ApiResponse<VendorSummaryResponse>> getVendorSummary(
            @PathVariable Long vendorId) {

        VendorSummaryResponse response = vendorService.getVendorSummary(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor summary fetched successfully", response));
    }
}