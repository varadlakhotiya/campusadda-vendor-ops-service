package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private Long id;
    private Long vendorId;
    private String orderNumber;
    private String sourceSystem;
    private String externalOrderId;
    private String externalCustomerId;
    private String orderSource;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime pickupEtaAt;
    private LocalDateTime placedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}