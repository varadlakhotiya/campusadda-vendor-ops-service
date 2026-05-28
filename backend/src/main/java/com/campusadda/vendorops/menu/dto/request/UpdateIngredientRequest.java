package com.campusadda.vendorops.menu.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateIngredientRequest {
    private BigDecimal quantityRequired;
    private BigDecimal wastagePct;
    private Boolean isOptional;
    private Boolean isActive;
}