package com.campusadda.vendorops.menu.repository;

import com.campusadda.vendorops.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByVendor_IdOrderByDisplayOrderAsc(Long vendorId);
    List<MenuItem> findByVendor_IdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Long vendorId);
    boolean existsByVendor_IdAndItemCode(Long vendorId, String itemCode);
    Optional<MenuItem> findByIdAndVendor_Id(Long id, Long vendorId);
}