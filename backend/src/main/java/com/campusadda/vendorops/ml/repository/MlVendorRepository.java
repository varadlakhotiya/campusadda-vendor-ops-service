package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlVendorRepository extends JpaRepository<Vendor, Long> {
    boolean existsByVendorCode(String vendorCode);
}