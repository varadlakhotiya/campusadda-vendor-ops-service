package com.campusadda.vendorops.menu.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class IngredientResponse {
    private Long id;
    private Long menuItemId;
    private Long inventoryItemId;
    private String inventoryItemCode;
    private String inventoryItemName;
    private String inventoryUnit;
    private BigDecimal quantityRequired;
    private BigDecimal wastagePct;
    private Boolean isOptional;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}