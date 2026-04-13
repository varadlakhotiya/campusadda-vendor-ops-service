package com.campusadda.vendorops.onboarding.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.onboarding.dto.request.CreateVendorSignupRequest;
import com.campusadda.vendorops.onboarding.dto.response.VendorSignupRequestResponse;
import com.campusadda.vendorops.onboarding.service.VendorSignupRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/vendor-signup-requests")
@RequiredArgsConstructor
public class PublicVendorSignupController {

    private final VendorSignupRequestService vendorSignupRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorSignupRequestResponse>> create(@RequestBody CreateVendorSignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor signup request submitted successfully",
                vendorSignupRequestService.create(request)
        ));
    }
}