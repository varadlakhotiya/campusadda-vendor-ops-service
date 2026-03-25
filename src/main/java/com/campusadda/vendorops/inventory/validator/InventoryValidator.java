package com.campusadda.vendorops.inventory.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryValidator {

    private final InventoryItemRepository inventoryItemRepository;

    public void validateUniqueItemCode(Long vendorId, String itemCode) {
        if (inventoryItemRepository.existsByVendor_IdAndItemCode(vendorId, itemCode)) {
            throw new ConflictException("Inventory item code already exists for this vendor");
        }
    }

    public InventoryItem validateInventoryItemExists(Long vendorId, Long inventoryItemId) {
        return inventoryItemRepository.findByIdAndVendor_Id(inventoryItemId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }
}