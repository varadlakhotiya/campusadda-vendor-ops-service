package com.campusadda.vendorops.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StockMovementResponse {
    private Long id;
    private Long vendorId;
    private Long inventoryItemId;
    private String inventoryItemName;
    private String movementType;
    private String referenceType;
    private Long referenceId;
    private BigDecimal quantityDelta;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private BigDecimal unitCost;
    private String reason;
    private String detailsJson;
    private Long createdByUserId;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}