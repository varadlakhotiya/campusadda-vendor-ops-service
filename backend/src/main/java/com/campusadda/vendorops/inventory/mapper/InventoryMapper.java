package com.campusadda.vendorops.inventory.mapper;

import com.campusadda.vendorops.inventory.dto.request.CreateInventoryItemRequest;
import com.campusadda.vendorops.inventory.dto.request.UpdateInventoryItemRequest;
import com.campusadda.vendorops.inventory.dto.response.InventoryItemResponse;
import com.campusadda.vendorops.inventory.dto.response.InventoryPolicyResponse;
import com.campusadda.vendorops.inventory.dto.response.LowStockItemResponse;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.InventoryPolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InventoryMapper {

    public InventoryItem toEntity(CreateInventoryItemRequest request) {
        InventoryItem item = new InventoryItem();
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setUnit(request.getUnit());
        item.setCurrentQuantity(request.getCurrentQuantity() != null ? request.getCurrentQuantity() : BigDecimal.ZERO);
        item.setReservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : BigDecimal.ZERO);
        item.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : BigDecimal.ZERO);
        item.setMaxStockLevel(request.getMaxStockLevel());
        item.setUnitCost(request.getUnitCost());
        item.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        item.setSourceSystem(request.getSourceSystem() != null ? request.getSourceSystem() : "VENDOR_OPS");
        item.setExternalInventoryItemId(request.getExternalInventoryItemId());
        return item;
    }

    public void updateEntity(InventoryItem item, UpdateInventoryItemRequest request) {
        if (request.getItemName() != null) item.setItemName(request.getItemName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getReservedQuantity() != null) item.setReservedQuantity(request.getReservedQuantity());
        if (request.getLowStockThreshold() != null) item.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getMaxStockLevel() != null) item.setMaxStockLevel(request.getMaxStockLevel());
        if (request.getUnitCost() != null) item.setUnitCost(request.getUnitCost());
        if (request.getExternalInventoryItemId() != null) item.setExternalInventoryItemId(request.getExternalInventoryItemId());
    }

    public InventoryItemResponse toResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .vendorId(item.getVendor().getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .description(item.getDescription())
                .unit(item.getUnit())
                .currentQuantity(item.getCurrentQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .maxStockLevel(item.getMaxStockLevel())
                .unitCost(item.getUnitCost())
                .status(item.getStatus())
                .lastRestockedAt(item.getLastRestockedAt())
                .sourceSystem(item.getSourceSystem())
                .externalInventoryItemId(item.getExternalInventoryItemId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public InventoryPolicyResponse toPolicyResponse(InventoryPolicy policy) {
        return InventoryPolicyResponse.builder()
                .id(policy.getId())
                .inventoryItemId(policy.getInventoryItem().getId())
                .leadTimeDays(policy.getLeadTimeDays())
                .reviewPeriodDays(policy.getReviewPeriodDays())
                .serviceLevelPct(policy.getServiceLevelPct())
                .safetyStockQty(policy.getSafetyStockQty())
                .reorderPointQty(policy.getReorderPointQty())
                .minReorderQty(policy.getMinReorderQty())
                .maxReorderQty(policy.getMaxReorderQty())
                .preferredModel(policy.getPreferredModel())
                .autoRecommendEnabled(policy.getAutoRecommendEnabled())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    public LowStockItemResponse toLowStockResponse(InventoryItem item) {
        return LowStockItemResponse.builder()
                .inventoryItemId(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .unit(item.getUnit())
                .currentQuantity(item.getCurrentQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .build();
    }
}