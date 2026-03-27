package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateOrderItemRequest;
import com.campusadda.vendorops.order.service.impl.OrderPricingServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPricingServiceImplTest {

    private final OrderPricingServiceImpl service = new OrderPricingServiceImpl();

    @Test
    void shouldCalculateSubtotalCorrectly() {
        MenuItem burger = new MenuItem();
        burger.setPrice(new BigDecimal("50.00"));

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setMenuItemId(1L);
        item.setQuantity(2);

        BigDecimal subtotal = service.calculateSubtotal(
                List.of(item),
                Map.of(1L, burger)
        );

        assertEquals(new BigDecimal("100.00"), subtotal);
    }
}