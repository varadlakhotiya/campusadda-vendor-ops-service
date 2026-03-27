package com.campusadda.vendorops.inventory.repository;

import com.campusadda.vendorops.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByInventoryItem_IdOrderByEventTimeDesc(Long inventoryItemId);
    List<StockMovement> findByVendor_IdOrderByEventTimeDesc(Long vendorId);
}