package com.campusadda.vendorops.inventory.repository;

import com.campusadda.vendorops.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByVendor_IdOrderByItemNameAsc(Long vendorId);
    Optional<InventoryItem> findByIdAndVendor_Id(Long id, Long vendorId);
    boolean existsByVendor_IdAndItemCode(Long vendorId, String itemCode);
    List<InventoryItem> findByVendor_IdAndCurrentQuantityLessThanEqual(Long vendorId, BigDecimal quantity);
}