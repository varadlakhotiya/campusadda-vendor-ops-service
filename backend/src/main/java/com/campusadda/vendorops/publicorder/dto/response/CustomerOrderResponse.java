package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerOrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long vendorId;
    private String vendorName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
}