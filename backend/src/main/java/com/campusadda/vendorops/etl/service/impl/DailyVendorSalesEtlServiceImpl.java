package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.DailyVendorSales;
import com.campusadda.vendorops.analytics.repository.DailyVendorSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.DailyVendorSalesEtlService;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyVendorSalesEtlServiceImpl implements DailyVendorSalesEtlService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DailyVendorSalesRepository dailyVendorSalesRepository;

    @Override
    public int aggregate(LocalDateTime windowStart, LocalDateTime windowEnd, EtlJobRun etlJobRun) {
        List<Order> sourceOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt() != null)
                .filter(order -> !order.getPlacedAt().isBefore(windowStart))
                .filter(order -> order.getPlacedAt().isBefore(windowEnd))
                .toList();

        Map<String, VendorAgg> aggregates = new LinkedHashMap<>();

        for (Order order : sourceOrders) {
            if (order.getVendor() == null) {
                continue;
            }

            String key = buildKey(order.getPlacedAt().toLocalDate().toString(), String.valueOf(order.getVendor().getId()));
            VendorAgg agg = aggregates.computeIfAbsent(key, ignored -> new VendorAgg());
            agg.salesDate = order.getPlacedAt().toLocalDate();
            agg.vendor = order.getVendor();
            agg.totalOrders++;

            if (isCompleted(order.getStatus())) {
                agg.completedOrders++;
                agg.grossRevenue = agg.grossRevenue.add(safeMoney(order.getTotalAmount()));

                List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
                agg.itemsSoldQty += items.stream().mapToInt(item -> safeInt(item == null ? null : item.getQuantity())).sum();

                agg.firstOrderAt = minDateTime(agg.firstOrderAt, order.getPlacedAt());
                agg.lastOrderAt = maxDateTime(agg.lastOrderAt, order.getPlacedAt());
            } else if (isCancelled(order.getStatus())) {
                agg.cancelledOrders++;
            } else {
                // Count the order in totalOrders, but do not include it in revenue or completed counts.
            }
        }

        int processedOrders = sourceOrders.size();

        for (VendorAgg agg : aggregates.values()) {
            Optional<DailyVendorSales> existingOpt =
                    dailyVendorSalesRepository.findBySalesDateAndVendor_Id(
                            agg.salesDate,
                            agg.vendor.getId()
                    );

            DailyVendorSales row = existingOpt.orElseGet(DailyVendorSales::new);
            row.setSalesDate(agg.salesDate);
            row.setVendor(agg.vendor);
            row.setTotalOrders(agg.totalOrders);
            row.setCompletedOrders(agg.completedOrders);
            row.setCancelledOrders(agg.cancelledOrders);
            row.setItemsSoldQty(agg.itemsSoldQty);
            row.setGrossRevenue(agg.grossRevenue);
            row.setNetRevenue(agg.grossRevenue);
            row.setAvgOrderValue(agg.completedOrders == 0
                    ? BigDecimal.ZERO
                    : agg.grossRevenue.divide(BigDecimal.valueOf(agg.completedOrders), 2, RoundingMode.HALF_UP));
            row.setEtlRun(etlJobRun);

            dailyVendorSalesRepository.save(row);
        }

        return processedOrders;
    }

    private boolean isCompleted(String status) {
        return status != null && STATUS_COMPLETED.equalsIgnoreCase(status);
    }

    private boolean isCancelled(String status) {
        return status != null && STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    private String buildKey(String salesDate, String vendorId) {
        return salesDate + "|" + vendorId;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDateTime minDateTime(LocalDateTime current, LocalDateTime candidate) {
        if (current == null) {
            return candidate;
        }
        return candidate != null && candidate.isBefore(current) ? candidate : current;
    }

    private LocalDateTime maxDateTime(LocalDateTime current, LocalDateTime candidate) {
        if (current == null) {
            return candidate;
        }
        return candidate != null && candidate.isAfter(current) ? candidate : current;
    }

    private static class VendorAgg {
        private java.time.LocalDate salesDate;
        private Vendor vendor;
        private int totalOrders = 0;
        private int completedOrders = 0;
        private int cancelledOrders = 0;
        private int itemsSoldQty = 0;
        private BigDecimal grossRevenue = BigDecimal.ZERO;
        private LocalDateTime firstOrderAt;
        private LocalDateTime lastOrderAt;
    }
}
