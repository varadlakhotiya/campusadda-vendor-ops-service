package com.campusadda.vendorops.inventory.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateInventoryItemRequest {
    private String itemName;
    private String description;
    private String unit;
    private BigDecimal reservedQuantity;
    private BigDecimal lowStockThreshold;
    private BigDecimal maxStockLevel;
    private BigDecimal unitCost;
    private String externalInventoryItemId;
}