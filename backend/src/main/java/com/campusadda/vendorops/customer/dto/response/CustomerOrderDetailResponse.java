package com.campusadda.vendorops.customer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CustomerOrderDetailResponse {
    private Long orderId;
    private String orderNumber;
    private Long vendorId;
    private String vendorName;
    private String status;
    private String customerName;
    private String customerPhone;
    private String notes;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private List<CustomerOrderHistoryItemResponse> items;
    private List<CustomerOrderTimelineResponse> statusHistory;
}