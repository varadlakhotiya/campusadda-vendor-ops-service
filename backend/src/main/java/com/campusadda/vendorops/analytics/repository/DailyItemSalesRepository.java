package com.campusadda.vendorops.analytics.repository;

import com.campusadda.vendorops.analytics.entity.DailyItemSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyItemSalesRepository extends JpaRepository<DailyItemSales, Long> {
    Optional<DailyItemSales> findBySalesDateAndVendor_IdAndMenuItem_Id(LocalDate salesDate, Long vendorId, Long menuItemId);
    List<DailyItemSales> findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(Long vendorId, LocalDate fromDate, LocalDate toDate);
    List<DailyItemSales> findTop10ByVendor_IdAndSalesDateBetweenOrderByQuantitySoldDesc(Long vendorId, LocalDate fromDate, LocalDate toDate);
}