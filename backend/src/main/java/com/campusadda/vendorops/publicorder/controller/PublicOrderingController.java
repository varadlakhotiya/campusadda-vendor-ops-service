package com.campusadda.vendorops.publicorder.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.publicorder.dto.request.CustomerCreateOrderRequest;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderTrackingResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorMenuResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorResponse;
import com.campusadda.vendorops.publicorder.service.PublicOrderingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicOrderingController {

    private final PublicOrderingService publicOrderingService;

    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<List<PublicVendorResponse>>> getActiveVendors() {
        return ResponseEntity.ok(ApiResponse.success(
                "Active vendors fetched successfully",
                publicOrderingService.getActiveVendors()
        ));
    }

    @GetMapping("/vendors/{vendorId}/menu")
    public ResponseEntity<ApiResponse<PublicVendorMenuResponse>> getVendorMenu(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor menu fetched successfully",
                publicOrderingService.getVendorMenu(vendorId)
        ));
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<CustomerOrderResponse>> placeOrder(
            @Valid @RequestBody CustomerCreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order placed successfully",
                publicOrderingService.placeOrder(request)
        ));
    }

    @GetMapping("/orders/track")
    public ResponseEntity<ApiResponse<CustomerOrderTrackingResponse>> trackOrder(
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("phone") String customerPhone) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order tracking details fetched successfully",
                publicOrderingService.trackOrder(orderNumber, customerPhone)
        ));
    }
}