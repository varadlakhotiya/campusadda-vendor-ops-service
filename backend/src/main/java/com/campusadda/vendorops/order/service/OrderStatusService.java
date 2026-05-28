package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.order.dto.request.CancelOrderRequest;
import com.campusadda.vendorops.order.dto.request.UpdateOrderStatusRequest;
import com.campusadda.vendorops.order.dto.response.OrderResponse;
import com.campusadda.vendorops.order.dto.response.OrderStatusHistoryResponse;

import java.util.List;

public interface OrderStatusService {
    OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request);
    OrderResponse cancelOrder(Long orderId, CancelOrderRequest request);
    List<OrderStatusHistoryResponse> getStatusHistory(Long orderId);
}