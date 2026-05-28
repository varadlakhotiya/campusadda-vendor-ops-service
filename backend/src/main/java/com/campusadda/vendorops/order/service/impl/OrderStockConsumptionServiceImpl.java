package com.campusadda.vendorops.order.service.impl;

import com.campusadda.vendorops.alert.service.LowStockAlertService;
import com.campusadda.vendorops.common.enums.MovementType;
import com.campusadda.vendorops.common.exception.BusinessException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.StockMovement;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import com.campusadda.vendorops.inventory.repository.StockMovementRepository;
import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import com.campusadda.vendorops.menu.repository.MenuItemIngredientRepository;
import com.campusadda.vendorops.order.dto.response.StockConsumptionPreviewResponse;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.order.service.OrderStockConsumptionService;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderStockConsumptionServiceImpl implements OrderStockConsumptionService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemIngredientRepository ingredientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final LowStockAlertService lowStockAlertService;
    private final VendorAccessService vendorAccessService;

    @Override
    @Transactional(readOnly = true)
    public StockConsumptionPreviewResponse previewStockConsumption(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        vendorAccessService.validateVendorAccess(order.getVendor().getId());

        List<String> issues = new ArrayList<>();
        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getMenuItem() == null || !Boolean.TRUE.equals(orderItem.getMenuItem().getTrackInventory())) {
                continue;
            }

            List<MenuItemIngredient> ingredients = getConsumableIngredients(orderItem.getMenuItem().getId());

            for (MenuItemIngredient ingredient : ingredients) {
                BigDecimal perUnitRequired = safe(ingredient.getQuantityRequired());

                if (perUnitRequired.compareTo(BigDecimal.ZERO) <= 0) {
                    issues.add("Invalid recipe quantity for " + ingredient.getInventoryItem().getItemName());
                    continue;
                }

                BigDecimal required = perUnitRequired.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                BigDecimal current = safe(ingredient.getInventoryItem().getCurrentQuantity());

                if (current.compareTo(required) < 0) {
                    issues.add("Insufficient stock for " + ingredient.getInventoryItem().getItemName());
                }
            }
        }

        return StockConsumptionPreviewResponse.builder()
                .orderId(orderId)
                .canConsume(issues.isEmpty())
                .issues(issues)
                .build();
    }

    @Override
    public void consumeStock(Order order) {
        vendorAccessService.validateVendorAccess(order.getVendor().getId());

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
        User currentUser = resolveCurrentUser();

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getMenuItem() == null || !Boolean.TRUE.equals(orderItem.getMenuItem().getTrackInventory())) {
                continue;
            }

            List<MenuItemIngredient> ingredients = getConsumableIngredients(orderItem.getMenuItem().getId());

            for (MenuItemIngredient ingredient : ingredients) {
                InventoryItem inventoryItem = ingredient.getInventoryItem();

                BigDecimal perUnitRequired = safe(ingredient.getQuantityRequired());
                if (perUnitRequired.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Invalid recipe quantity for " + inventoryItem.getItemName());
                }

                BigDecimal required = perUnitRequired.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                BigDecimal before = safe(inventoryItem.getCurrentQuantity());
                BigDecimal after = before.subtract(required);

                if (after.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("Insufficient stock for " + inventoryItem.getItemName());
                }

                inventoryItem.setCurrentQuantity(after);
                inventoryItemRepository.save(inventoryItem);

                StockMovement movement = new StockMovement();
                movement.setVendor(order.getVendor());
                movement.setInventoryItem(inventoryItem);
                movement.setMovementType(MovementType.CONSUMPTION.name());
                movement.setReferenceType("ORDER");
                movement.setReferenceId(order.getId());
                movement.setQuantityDelta(required.negate());
                movement.setQuantityBefore(before);
                movement.setQuantityAfter(after);
                movement.setUnitCost(inventoryItem.getUnitCost());
                movement.setReason("Order accepted: " + order.getOrderNumber());
                movement.setCreatedByUser(currentUser);
                movement.setEventTime(LocalDateTime.now());
                stockMovementRepository.save(movement);

                lowStockAlertService.checkAndCreateLowStockAlert(inventoryItem);
            }
        }
    }

    @Override
    public void reverseStock(Order order) {
        vendorAccessService.validateVendorAccess(order.getVendor().getId());

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
        User currentUser = resolveCurrentUser();

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getMenuItem() == null || !Boolean.TRUE.equals(orderItem.getMenuItem().getTrackInventory())) {
                continue;
            }

            List<MenuItemIngredient> ingredients = getConsumableIngredients(orderItem.getMenuItem().getId());

            for (MenuItemIngredient ingredient : ingredients) {
                InventoryItem inventoryItem = ingredient.getInventoryItem();

                BigDecimal perUnitRequired = safe(ingredient.getQuantityRequired());
                if (perUnitRequired.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal restore = perUnitRequired.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                BigDecimal before = safe(inventoryItem.getCurrentQuantity());
                BigDecimal after = before.add(restore);

                inventoryItem.setCurrentQuantity(after);
                inventoryItemRepository.save(inventoryItem);

                StockMovement movement = new StockMovement();
                movement.setVendor(order.getVendor());
                movement.setInventoryItem(inventoryItem);
                movement.setMovementType(MovementType.RETURN.name());
                movement.setReferenceType("ORDER_CANCEL");
                movement.setReferenceId(order.getId());
                movement.setQuantityDelta(restore);
                movement.setQuantityBefore(before);
                movement.setQuantityAfter(after);
                movement.setUnitCost(inventoryItem.getUnitCost());
                movement.setReason("Order cancelled: " + order.getOrderNumber());
                movement.setCreatedByUser(currentUser);
                movement.setEventTime(LocalDateTime.now());
                stockMovementRepository.save(movement);
            }
        }
    }

    private List<MenuItemIngredient> getConsumableIngredients(Long menuItemId) {
        return ingredientRepository.findByMenuItem_Id(menuItemId)
                .stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .filter(i -> !Boolean.TRUE.equals(i.getIsOptional()))
                .toList();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private User resolveCurrentUser() {
        try {
            Long currentUserId = securityUtils.getCurrentUserId();
            return userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        } catch (Exception ex) {
            return null;
        }
    }
}