package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MlInventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByIdAndVendor_Id(Long inventoryItemId, Long vendorId);
}