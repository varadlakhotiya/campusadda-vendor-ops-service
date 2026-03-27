package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {
    private OrderResponse order;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;
}