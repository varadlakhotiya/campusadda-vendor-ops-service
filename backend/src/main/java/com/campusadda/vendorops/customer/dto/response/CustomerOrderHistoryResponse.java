package com.campusadda.vendorops.customer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CustomerOrderHistoryResponse {
    private Long orderId;
    private String orderNumber;
    private Long vendorId;
    private String vendorName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private List<CustomerOrderHistoryItemResponse> items;
}