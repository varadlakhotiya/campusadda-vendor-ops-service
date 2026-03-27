package com.campusadda.vendorops.order.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.order.dto.request.CreateManualOrderRequest;
import com.campusadda.vendorops.order.dto.request.CreateOrderRequest;
import com.campusadda.vendorops.order.dto.response.OrderBoardResponse;
import com.campusadda.vendorops.order.dto.response.OrderDetailResponse;
import com.campusadda.vendorops.order.dto.response.OrderResponse;
import com.campusadda.vendorops.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderService.createOrder(request)));
    }

    @PostMapping("/vendors/{vendorId}/orders/manual")
    public ResponseEntity<ApiResponse<OrderResponse>> createManualOrder(
            @PathVariable Long vendorId,
            @Valid @RequestBody CreateManualOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manual order created successfully",
                        orderService.createManualOrder(vendorId, request)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully",
                orderService.getOrderById(orderId)));
    }

    @GetMapping("/vendors/{vendorId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getVendorOrders(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully",
                orderService.getOrders(vendorId)));
    }

    @GetMapping("/vendors/{vendorId}/orders/board")
    public ResponseEntity<ApiResponse<OrderBoardResponse>> getOrderBoard(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Order board fetched successfully",
                orderService.getOrderBoard(vendorId)));
    }
}