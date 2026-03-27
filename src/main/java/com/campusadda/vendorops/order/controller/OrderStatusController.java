package com.campusadda.vendorops.order.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.order.dto.request.CancelOrderRequest;
import com.campusadda.vendorops.order.dto.request.UpdateOrderStatusRequest;
import com.campusadda.vendorops.order.dto.response.OrderResponse;
import com.campusadda.vendorops.order.dto.response.OrderStatusHistoryResponse;
import com.campusadda.vendorops.order.service.OrderStatusService;
import com.campusadda.vendorops.order.service.OrderStockConsumptionService;
import com.campusadda.vendorops.order.dto.response.StockConsumptionPreviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderStatusController {

    private final OrderStatusService orderStatusService;
    private final OrderStockConsumptionService orderStockConsumptionService;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully",
                orderStatusService.updateStatus(orderId, request)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully",
                orderStatusService.cancelOrder(orderId, request)));
    }

    @GetMapping("/{orderId}/status-history")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getStatusHistory(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order status history fetched successfully",
                orderStatusService.getStatusHistory(orderId)));
    }

    @GetMapping("/{orderId}/stock-consumption-preview")
    public ResponseEntity<ApiResponse<StockConsumptionPreviewResponse>> previewStockConsumption(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Stock consumption preview generated",
                orderStockConsumptionService.previewStockConsumption(orderId)));
    }
}