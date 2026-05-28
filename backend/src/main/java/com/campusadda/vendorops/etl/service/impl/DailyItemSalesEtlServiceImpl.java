package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.DailyItemSales;
import com.campusadda.vendorops.analytics.repository.DailyItemSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.DailyItemSalesEtlService;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt() != null
                        && !order.getPlacedAt().isBefore(windowStart)
                        && !order.getPlacedAt().isAfter(windowEnd))
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .toList();

        int processed = 0;

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());

            for (OrderItem item : items) {
                if (item.getMenuItem() == null) continue;

                Optional<DailyItemSales> existingOpt =
                        dailyItemSalesRepository.findBySalesDateAndVendor_IdAndMenuItem_Id(
                                order.getPlacedAt().toLocalDate(),
                                order.getVendor().getId(),
                                item.getMenuItem().getId()
                        );

                DailyItemSales row = existingOpt.orElseGet(DailyItemSales::new);
                row.setSalesDate(order.getPlacedAt().toLocalDate());
                row.setVendor(order.getVendor());
                row.setMenuItem(item.getMenuItem());
                row.setQuantitySold((row.getQuantitySold() == null ? 0 : row.getQuantitySold()) + item.getQuantity());
                row.setOrderCount((row.getOrderCount() == null ? 0 : row.getOrderCount()) + 1);
                row.setGrossRevenue((row.getGrossRevenue() == null ? BigDecimal.ZERO : row.getGrossRevenue()).add(item.getLineTotal()));
                row.setNetRevenue(row.getGrossRevenue());
                row.setAvgSellingPrice(
                        row.getQuantitySold() == 0 ? BigDecimal.ZERO :
                                row.getGrossRevenue().divide(BigDecimal.valueOf(row.getQuantitySold()), 2, java.math.RoundingMode.HALF_UP)
                );
                row.setFirstOrderAt(row.getFirstOrderAt() == null ? order.getPlacedAt() : row.getFirstOrderAt());
                row.setLastOrderAt(order.getPlacedAt());
                row.setEtlRun(etlJobRun);

                dailyItemSalesRepository.save(row);
                processed++;
            }
        }

        return processed;
    }
}