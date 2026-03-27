package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.DailyVendorSales;
import com.campusadda.vendorops.analytics.repository.DailyVendorSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.DailyVendorSalesEtlService;
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
public class DailyVendorSalesEtlServiceImpl implements DailyVendorSalesEtlService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DailyVendorSalesRepository dailyVendorSalesRepository;

    @Override
    public int aggregate(LocalDateTime windowStart, LocalDateTime windowEnd, EtlJobRun etlJobRun) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt() != null
                        && !order.getPlacedAt().isBefore(windowStart)
                        && !order.getPlacedAt().isAfter(windowEnd))
                .toList();

        int processed = 0;

        for (Order order : orders) {
            Optional<DailyVendorSales> existingOpt =
                    dailyVendorSalesRepository.findBySalesDateAndVendor_Id(
                            order.getPlacedAt().toLocalDate(),
                            order.getVendor().getId()
                    );

            DailyVendorSales row = existingOpt.orElseGet(DailyVendorSales::new);
            row.setSalesDate(order.getPlacedAt().toLocalDate());
            row.setVendor(order.getVendor());
            row.setTotalOrders((row.getTotalOrders() == null ? 0 : row.getTotalOrders()) + 1);

            if ("COMPLETED".equals(order.getStatus())) {
                row.setCompletedOrders((row.getCompletedOrders() == null ? 0 : row.getCompletedOrders()) + 1);
            }
            if ("CANCELLED".equals(order.getStatus())) {
                row.setCancelledOrders((row.getCancelledOrders() == null ? 0 : row.getCancelledOrders()) + 1);
            }

            List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
            int itemsQty = items.stream().mapToInt(OrderItem::getQuantity).sum();

            row.setItemsSoldQty((row.getItemsSoldQty() == null ? 0 : row.getItemsSoldQty()) + itemsQty);
            row.setGrossRevenue((row.getGrossRevenue() == null ? BigDecimal.ZERO : row.getGrossRevenue()).add(order.getTotalAmount()));
            row.setNetRevenue(row.getGrossRevenue());
            row.setAvgOrderValue(
                    row.getTotalOrders() == 0 ? BigDecimal.ZERO :
                            row.getGrossRevenue().divide(BigDecimal.valueOf(row.getTotalOrders()), 2, java.math.RoundingMode.HALF_UP)
            );
            row.setEtlRun(etlJobRun);

            dailyVendorSalesRepository.save(row);
            processed++;
        }

        return processed;
    }
}