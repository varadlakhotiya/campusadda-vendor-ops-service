package com.campusadda.vendorops.onboarding.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.onboarding.dto.request.ApproveVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.request.RejectVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.response.VendorSignupRequestResponse;
import com.campusadda.vendorops.onboarding.service.VendorSignupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/vendor-signup-requests")
@RequiredArgsConstructor
public class AdminVendorSignupController {

    private final VendorSignupRequestService vendorSignupRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorSignupRequestResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor signup requests fetched successfully",
                vendorSignupRequestService.listAll()
        ));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<VendorSignupRequestResponse>>> listPending() {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending vendor signup requests fetched successfully",
                vendorSignupRequestService.listPending()
        ));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<VendorSignupRequestResponse>> approve(
            @PathVariable Long requestId,
            @RequestBody ApproveVendorSignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor signup request approved successfully",
                vendorSignupRequestService.approve(requestId, request)
        ));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<VendorSignupRequestResponse>> reject(
            @PathVariable Long requestId,
            @RequestBody RejectVendorSignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor signup request rejected successfully",
                vendorSignupRequestService.reject(requestId, request)
        ));
    }
}