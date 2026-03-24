package com.campusadda.vendorops.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockInRequest {
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String reason;
}