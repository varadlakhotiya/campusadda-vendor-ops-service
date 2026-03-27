package com.campusadda.vendorops.analytics.repository;

import com.campusadda.vendorops.analytics.entity.HourlyVendorSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HourlyVendorSalesRepository extends JpaRepository<HourlyVendorSales, Long> {
    Optional<HourlyVendorSales> findBySalesDateAndVendor_IdAndSalesHour(LocalDate salesDate, Long vendorId, Integer salesHour);
    List<HourlyVendorSales> findByVendor_IdAndSalesDateBetweenOrderBySalesDateAscSalesHourAsc(Long vendorId, LocalDate fromDate, LocalDate toDate);
}