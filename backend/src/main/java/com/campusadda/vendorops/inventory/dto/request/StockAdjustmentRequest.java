package com.campusadda.vendorops.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockAdjustmentRequest {
    @NotNull(message = "Adjusted quantity is required")
    private BigDecimal adjustedQuantity;
    private String reason;
}