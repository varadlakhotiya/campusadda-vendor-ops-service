package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderStatusHistoryResponse {
    private Long id;
    private Long orderId;
    private String fromStatus;
    private String toStatus;
    private Long changedByUserId;
    private String remarks;
    private LocalDateTime changedAt;
}