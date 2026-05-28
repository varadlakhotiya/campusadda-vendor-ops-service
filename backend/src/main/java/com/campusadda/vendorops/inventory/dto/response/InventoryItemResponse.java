package com.campusadda.vendorops.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryItemResponse {
    private Long id;
    private Long vendorId;
    private String itemCode;
    private String itemName;
    private String description;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal lowStockThreshold;
    private BigDecimal maxStockLevel;
    private BigDecimal unitCost;
    private String status;
    private LocalDateTime lastRestockedAt;
    private String sourceSystem;
    private String externalInventoryItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}