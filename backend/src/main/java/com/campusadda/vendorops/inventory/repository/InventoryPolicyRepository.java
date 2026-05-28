package com.campusadda.vendorops.inventory.repository;

import com.campusadda.vendorops.inventory.entity.InventoryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryPolicyRepository extends JpaRepository<InventoryPolicy, Long> {
    Optional<InventoryPolicy> findByInventoryItem_Id(Long inventoryItemId);
}