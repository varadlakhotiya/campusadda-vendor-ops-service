package com.campusadda.vendorops.order.validator;

import com.campusadda.vendorops.common.exception.BusinessException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.validator.MenuItemValidator;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final OrderRepository orderRepository;
    private final MenuItemValidator menuItemValidator;

    public Order validateOrderExists(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public Order validateOrderExistsForVendor(Long vendorId, Long orderId) {
        return orderRepository.findByIdAndVendor_Id(orderId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public MenuItem validateOrderableMenuItem(Long vendorId, Long menuItemId) {
        MenuItem item = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        if (!Boolean.TRUE.equals(item.getIsActive())) {
            throw new BusinessException("Menu item is inactive: " + item.getItemName());
        }

        if (!Boolean.TRUE.equals(item.getIsAvailable())) {
            throw new BusinessException("Menu item is unavailable: " + item.getItemName());
        }

        return item;
    }
}