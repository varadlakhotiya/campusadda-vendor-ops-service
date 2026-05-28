package com.campusadda.vendorops.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private Long orderId;
    private Long menuItemId;
    private String itemNameSnapshot;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private String specialInstructions;
    private String recipeSnapshotJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}