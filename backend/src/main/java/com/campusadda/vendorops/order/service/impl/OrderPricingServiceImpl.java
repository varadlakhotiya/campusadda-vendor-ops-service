package com.campusadda.vendorops.order.service.impl;

import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateOrderItemRequest;
import com.campusadda.vendorops.order.service.OrderPricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderPricingServiceImpl implements OrderPricingService {

    @Override
    public BigDecimal calculateSubtotal(List<CreateOrderItemRequest> items, Map<Long, MenuItem> menuItemsById) {
        return items.stream()
                .map(item -> {
                    MenuItem menuItem = menuItemsById.get(item.getMenuItemId());
                    return menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal discount) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal discount, BigDecimal tax) {
        return subtotal.subtract(discount).add(tax);
    }
}