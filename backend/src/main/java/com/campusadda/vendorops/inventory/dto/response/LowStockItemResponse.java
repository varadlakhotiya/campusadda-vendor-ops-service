package com.campusadda.vendorops.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LowStockItemResponse {
    private Long inventoryItemId;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal lowStockThreshold;
}