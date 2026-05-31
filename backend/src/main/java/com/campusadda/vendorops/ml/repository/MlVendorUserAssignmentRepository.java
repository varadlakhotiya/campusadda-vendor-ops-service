package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MlVendorUserAssignmentRepository extends JpaRepository<VendorUserAssignment, Long> {
    boolean existsByVendor_IdAndUser_Id(Long vendorId, Long userId);

    List<VendorUserAssignment> findByVendor_Id(Long vendorId);

    List<VendorUserAssignment> findByUser_Id(Long userId);

    void deleteByVendor_IdAndUser_Id(Long vendorId, Long userId);
}