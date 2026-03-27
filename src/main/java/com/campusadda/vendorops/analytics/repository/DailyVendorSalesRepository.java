package com.campusadda.vendorops.analytics.repository;

import com.campusadda.vendorops.analytics.entity.DailyVendorSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyVendorSalesRepository extends JpaRepository<DailyVendorSales, Long> {
    Optional<DailyVendorSales> findBySalesDateAndVendor_Id(LocalDate salesDate, Long vendorId);
    List<DailyVendorSales> findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(Long vendorId, LocalDate fromDate, LocalDate toDate);
}