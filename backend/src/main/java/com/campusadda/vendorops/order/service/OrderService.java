package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.order.dto.request.CreateManualOrderRequest;
import com.campusadda.vendorops.order.dto.request.CreateOrderRequest;
import com.campusadda.vendorops.order.dto.response.OrderBoardResponse;
import com.campusadda.vendorops.order.dto.response.OrderDetailResponse;
import com.campusadda.vendorops.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse createManualOrder(Long vendorId, CreateManualOrderRequest request);
    OrderDetailResponse getOrderById(Long orderId);
    List<OrderResponse> getOrders(Long vendorId);
    OrderBoardResponse getOrderBoard(Long vendorId);
}