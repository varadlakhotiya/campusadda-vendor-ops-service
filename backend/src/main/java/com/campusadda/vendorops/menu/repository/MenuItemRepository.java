package com.campusadda.vendorops.menu.repository;

import com.campusadda.vendorops.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByVendor_IdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(Long vendorId);

    List<MenuItem> findByVendor_IdAndCategory_IdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(
            Long vendorId,
            Long categoryId
    );

    List<MenuItem> findByVendor_IdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(Long vendorId);

    boolean existsByVendor_IdAndItemCode(Long vendorId, String itemCode);

    Optional<MenuItem> findByIdAndVendor_Id(Long id, Long vendorId);

    List<MenuItem> findByVendor_IdAndIdInAndIsActiveTrueAndIsAvailableTrue(Long vendorId, Collection<Long> ids);

    long countByVendor_IdAndIsActiveTrue(Long vendorId);
}