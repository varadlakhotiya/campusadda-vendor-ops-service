package com.campusadda.vendorops.customer.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderDetailResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderHistoryResponse;
import com.campusadda.vendorops.customer.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerOrderHistoryResponse>>> getMyOrders() {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer orders fetched successfully",
                customerOrderService.getMyOrders()
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CustomerOrderHistoryResponse>>> getMyActiveOrders() {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer active orders fetched successfully",
                customerOrderService.getMyActiveOrders()
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<CustomerOrderDetailResponse>> getMyOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer order fetched successfully",
                customerOrderService.getMyOrder(orderId)
        ));
    }
}