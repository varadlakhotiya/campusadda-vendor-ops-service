package com.campusadda.vendorops.ml.repository;

import com.campusadda.vendorops.analytics.entity.DailyItemSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MlDailyItemSalesRepository extends JpaRepository<DailyItemSales, Long> {
    List<DailyItemSales> findByVendor_IdAndSalesDateBetweenOrderBySalesDateAsc(
            Long vendorId,
            LocalDate fromDate,
            LocalDate toDate
    );
}