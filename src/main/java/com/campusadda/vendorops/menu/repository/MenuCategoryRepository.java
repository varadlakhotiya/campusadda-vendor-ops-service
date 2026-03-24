package com.campusadda.vendorops.menu.repository;

import com.campusadda.vendorops.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByVendor_IdOrderByDisplayOrderAsc(Long vendorId);
    boolean existsByVendor_IdAndCategoryName(Long vendorId, String categoryName);
    Optional<MenuCategory> findByIdAndVendor_Id(Long id, Long vendorId);
}