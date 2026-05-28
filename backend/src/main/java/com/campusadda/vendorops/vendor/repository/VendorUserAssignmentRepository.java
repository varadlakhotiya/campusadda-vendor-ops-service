package com.campusadda.vendorops.vendor.repository;

import java.util.List;

import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorUserAssignmentRepository extends JpaRepository<VendorUserAssignment, Long> {

    List<VendorUserAssignment> findByUser_Id(Long userId);

    List<VendorUserAssignment> findByVendor_Id(Long vendorId);

    boolean existsByVendor_IdAndUser_Id(Long vendorId, Long userId);

    void deleteByVendor_IdAndUser_Id(Long vendorId, Long userId);
}