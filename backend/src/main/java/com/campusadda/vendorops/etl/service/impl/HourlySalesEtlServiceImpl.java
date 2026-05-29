package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.HourlyVendorSales;
import com.campusadda.vendorops.analytics.repository.HourlyVendorSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.HourlySalesEtlService;
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
public class HourlySalesEtlServiceImpl implements HourlySalesEtlService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HourlyVendorSalesRepository hourlyVendorSalesRepository;

    @Override
    public int aggregate(LocalDateTime windowStart, LocalDateTime windowEnd, EtlJobRun etlJobRun) {
        List<Order> sourceOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt() != null)
                .filter(order -> !order.getPlacedAt().isBefore(windowStart))
                .filter(order -> order.getPlacedAt().isBefore(windowEnd))
                .filter(order -> isCompleted(order.getStatus()))
                .toList();

        Map<String, HourlyAgg> aggregates = new LinkedHashMap<>();

        for (Order order : sourceOrders) {
            if (order.getVendor() == null) {
                continue;
            }

            int hour = order.getPlacedAt().getHour();
            String key = buildKey(order.getPlacedAt().toLocalDate().toString(),
                    String.valueOf(order.getVendor().getId()),
                    String.valueOf(hour));

            HourlyAgg agg = aggregates.computeIfAbsent(key, ignored -> new HourlyAgg());
            agg.salesDate = order.getPlacedAt().toLocalDate();
            agg.vendor = order.getVendor();
            agg.salesHour = (byte) hour;
            agg.totalOrders++;
            agg.itemsSoldQty += orderItemRepository.findByOrder_Id(order.getId()).stream()
                    .mapToInt(item -> safeInt(item == null ? null : item.getQuantity()))
                    .sum();
            agg.revenue = agg.revenue.add(safeMoney(order.getTotalAmount()));
        }

        int processedOrders = sourceOrders.size();

        for (HourlyAgg agg : aggregates.values()) {
            Optional<HourlyVendorSales> existingOpt =
                    hourlyVendorSalesRepository.findBySalesDateAndVendor_IdAndSalesHour(
                            agg.salesDate,
                            agg.vendor.getId(),
                            (int) agg.salesHour
                    );

            HourlyVendorSales row = existingOpt.orElseGet(HourlyVendorSales::new);
            row.setSalesDate(agg.salesDate);
            row.setVendor(agg.vendor);
            row.setSalesHour(agg.salesHour);
            row.setTotalOrders(agg.totalOrders);
            row.setItemsSoldQty(agg.itemsSoldQty);
            row.setRevenue(agg.revenue);
            row.setEtlRun(etlJobRun);

            hourlyVendorSalesRepository.save(row);
        }

        return processedOrders;
    }

    private boolean isCompleted(String status) {
        return status != null && "COMPLETED".equalsIgnoreCase(status);
    }

    private String buildKey(String salesDate, String vendorId, String salesHour) {
        return salesDate + "|" + vendorId + "|" + salesHour;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static class HourlyAgg {
        private java.time.LocalDate salesDate;
        private Vendor vendor;
        private byte salesHour;
        private int totalOrders = 0;
        private int itemsSoldQty = 0;
        private BigDecimal revenue = BigDecimal.ZERO;
    }
}
