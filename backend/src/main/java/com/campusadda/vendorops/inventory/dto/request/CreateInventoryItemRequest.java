package com.campusadda.vendorops.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateInventoryItemRequest {
    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    private String itemName;

    private String description;

    @NotBlank(message = "Unit is required")
    private String unit;

    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal lowStockThreshold;
    private BigDecimal maxStockLevel;
    private BigDecimal unitCost;
    private String status;
    private String sourceSystem;
    private String externalInventoryItemId;
}