package com.campusadda.vendorops.order.service.impl;

import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateOrderRequest;
import com.campusadda.vendorops.order.service.OrderValidationService;
import com.campusadda.vendorops.order.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderValidationServiceImpl implements OrderValidationService {

    private final OrderValidator orderValidator;

    @Override
    public Map<Long, MenuItem> validateAndLoadMenuItems(CreateOrderRequest request) {
        Map<Long, MenuItem> menuItemsById = new HashMap<>();

        request.getItems().forEach(itemRequest -> {
            MenuItem menuItem = orderValidator.validateOrderableMenuItem(
                    request.getVendorId(),
                    itemRequest.getMenuItemId()
            );
            menuItemsById.put(menuItem.getId(), menuItem);
        });

        return menuItemsById;
    }
}