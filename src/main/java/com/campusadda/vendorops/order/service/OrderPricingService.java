package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateOrderItemRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderPricingService {
    BigDecimal calculateSubtotal(List<CreateOrderItemRequest> items, Map<Long, MenuItem> menuItemsById);
    BigDecimal calculateDiscount(BigDecimal subtotal);
    BigDecimal calculateTax(BigDecimal subtotal, BigDecimal discount);
    BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal discount, BigDecimal tax);
}