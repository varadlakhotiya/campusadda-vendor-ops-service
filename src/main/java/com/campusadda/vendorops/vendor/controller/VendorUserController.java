package com.campusadda.vendorops.vendor.controller;

import java.util.List;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.vendor.dto.request.AssignVendorUserRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;
import com.campusadda.vendorops.vendor.service.VendorUserAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/users")
@RequiredArgsConstructor
public class VendorUserController {

    private final VendorUserAssignmentService vendorUserAssignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorUserAssignmentResponse>> assignUserToVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody AssignVendorUserRequest request) {

        VendorUserAssignmentResponse response =
                vendorUserAssignmentService.assignUserToVendor(vendorId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User assigned to vendor successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorUserAssignmentResponse>>> getVendorUsers(
            @PathVariable Long vendorId) {

        List<VendorUserAssignmentResponse> response =
                vendorUserAssignmentService.getVendorUsers(vendorId);

        return ResponseEntity.ok(ApiResponse.success("Vendor users fetched successfully", response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeUserFromVendor(
            @PathVariable Long vendorId,
            @PathVariable Long userId) {

        vendorUserAssignmentService.removeUserFromVendor(vendorId, userId);
        return ResponseEntity.ok(ApiResponse.success("User removed from vendor successfully", null));
    }
}