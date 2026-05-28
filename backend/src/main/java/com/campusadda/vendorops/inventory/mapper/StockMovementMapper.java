package com.campusadda.vendorops.inventory.mapper;

import com.campusadda.vendorops.inventory.dto.response.StockMovementResponse;
import com.campusadda.vendorops.inventory.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .vendorId(movement.getVendor().getId())
                .inventoryItemId(movement.getInventoryItem().getId())
                .inventoryItemName(movement.getInventoryItem().getItemName())
                .movementType(movement.getMovementType())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .quantityDelta(movement.getQuantityDelta())
                .quantityBefore(movement.getQuantityBefore())
                .quantityAfter(movement.getQuantityAfter())
                .unitCost(movement.getUnitCost())
                .reason(movement.getReason())
                .detailsJson(movement.getDetailsJson())
                .createdByUserId(movement.getCreatedByUser() != null ? movement.getCreatedByUser().getId() : null)
                .eventTime(movement.getEventTime())
                .createdAt(movement.getCreatedAt())
                .updatedAt(movement.getUpdatedAt())
                .build();
    }
}