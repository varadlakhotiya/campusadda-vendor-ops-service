package com.campusadda.vendorops.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateIngredientRequest {
    @NotNull(message = "Inventory item id is required")
    private Long inventoryItemId;

    @NotNull(message = "Quantity required is required")
    private BigDecimal quantityRequired;

    private BigDecimal wastagePct;
    private Boolean isOptional;
    private Boolean isActive;
}