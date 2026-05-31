package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MlMenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByIdAndVendor_Id(Long menuItemId, Long vendorId);

    List<MenuItem> findByVendor_IdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Long vendorId);
}