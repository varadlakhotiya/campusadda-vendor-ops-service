package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.DailyItemSales;
import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.DailyItemSalesEtlService;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.menu.entity.MenuItem;
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
public class DailyItemSalesEtlServiceImpl implements DailyItemSalesEtlService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DailyItemSalesRepository dailyItemSalesRepository;

    @Override
    public int aggregate(LocalDateTime windowStart, LocalDateTime windowEnd, EtlJobRun etlJobRun) {
        List<Order> sourceOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt() != null)
                .filter(order -> !order.getPlacedAt().isBefore(windowStart))
                .filter(order -> order.getPlacedAt().isBefore(windowEnd))
                .filter(order -> isCompleted(order.getStatus()))
                .toList();

        Map<String, ItemAgg> aggregates = new LinkedHashMap<>();

        for (Order order : sourceOrders) {
            if (order.getVendor() == null) {
                continue;
            }

            List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
            for (OrderItem item : items) {
                if (item == null || item.getMenuItem() == null) {
                    continue;
                }

                String key = buildKey(order.getPlacedAt().toLocalDate().toString(),
                        String.valueOf(order.getVendor().getId()),
                        String.valueOf(item.getMenuItem().getId()));

                ItemAgg agg = aggregates.computeIfAbsent(key, ignored -> new ItemAgg());
                agg.salesDate = order.getPlacedAt().toLocalDate();
                agg.vendor = order.getVendor();
                agg.menuItem = item.getMenuItem();
                agg.quantitySold += safeInt(item.getQuantity());
                agg.orderIds.add(order.getId());
                agg.grossRevenue = agg.grossRevenue.add(safeMoney(item.getLineTotal()));
                agg.firstOrderAt = minDateTime(agg.firstOrderAt, order.getPlacedAt());
                agg.lastOrderAt = maxDateTime(agg.lastOrderAt, order.getPlacedAt());
            }
        }

        int processedOrders = sourceOrders.size();

        for (ItemAgg agg : aggregates.values()) {
            Optional<DailyItemSales> existingOpt =
                    dailyItemSalesRepository.findBySalesDateAndVendor_IdAndMenuItem_Id(
                            agg.salesDate,
                            agg.vendor.getId(),
                            agg.menuItem.getId()
                    );

            DailyItemSales row = existingOpt.orElseGet(DailyItemSales::new);
            row.setSalesDate(agg.salesDate);
            row.setVendor(agg.vendor);
            row.setMenuItem(agg.menuItem);
            row.setQuantitySold(agg.quantitySold);
            row.setOrderCount(agg.orderIds.size());
            row.setGrossRevenue(agg.grossRevenue);
            row.setNetRevenue(agg.grossRevenue);
            row.setAvgSellingPrice(agg.quantitySold == 0
                    ? BigDecimal.ZERO
                    : agg.grossRevenue.divide(BigDecimal.valueOf(agg.quantitySold), 2, RoundingMode.HALF_UP));
            row.setFirstOrderAt(agg.firstOrderAt);
            row.setLastOrderAt(agg.lastOrderAt);
            row.setEtlRun(etlJobRun);

            dailyItemSalesRepository.save(row);
        }

        return processedOrders;
    }

    private boolean isCompleted(String status) {
        return status != null && "COMPLETED".equalsIgnoreCase(status);
    }

    private String buildKey(String salesDate, String vendorId, String menuItemId) {
        return salesDate + "|" + vendorId + "|" + menuItemId;
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

    private static class ItemAgg {
        private java.time.LocalDate salesDate;
        private Vendor vendor;
        private MenuItem menuItem;
        private int quantitySold = 0;
        private final java.util.Set<Long> orderIds = new java.util.LinkedHashSet<>();
        private BigDecimal grossRevenue = BigDecimal.ZERO;
        private LocalDateTime firstOrderAt;
        private LocalDateTime lastOrderAt;
    }
}
