package com.campusadda.vendorops.vendor.repository;

import java.util.Optional;

import com.campusadda.vendorops.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    boolean existsByVendorCode(String vendorCode);

    Optional<Vendor> findByIdAndStatus(Long id, String status);
}