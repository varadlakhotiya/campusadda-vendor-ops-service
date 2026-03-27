package com.campusadda.vendorops.etl.service.impl;

import com.campusadda.vendorops.analytics.entity.HourlyVendorSales;
import com.campusadda.vendorops.analytics.repository.HourlyVendorSalesRepository;
import com.campusadda.vendorops.etl.entity.EtlJobRun;
import com.campusadda.vendorops.etl.service.HourlySalesEtlService;
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
public class HourlySalesEtlServiceImpl implements HourlySalesEtlService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HourlyVendorSalesRepository hourlyVendorSalesRepository;

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
            int hour = order.getPlacedAt().getHour();

            Optional<HourlyVendorSales> existingOpt =
                    hourlyVendorSalesRepository.findBySalesDateAndVendor_IdAndSalesHour(
                            order.getPlacedAt().toLocalDate(),
                            order.getVendor().getId(),
                            hour
                    );

            HourlyVendorSales row = existingOpt.orElseGet(HourlyVendorSales::new);
            row.setSalesDate(order.getPlacedAt().toLocalDate());
            row.setVendor(order.getVendor());
            row.setSalesHour(hour);
            row.setTotalOrders((row.getTotalOrders() == null ? 0 : row.getTotalOrders()) + 1);

            List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
            int itemsQty = items.stream().mapToInt(OrderItem::getQuantity).sum();

            row.setItemsSoldQty((row.getItemsSoldQty() == null ? 0 : row.getItemsSoldQty()) + itemsQty);
            row.setRevenue((row.getRevenue() == null ? BigDecimal.ZERO : row.getRevenue()).add(order.getTotalAmount()));
            row.setEtlRun(etlJobRun);

            hourlyVendorSalesRepository.save(row);
            processed++;
        }

        return processed;
    }
}