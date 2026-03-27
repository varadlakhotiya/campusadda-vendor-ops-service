package com.campusadda.vendorops.order.mapper;

import com.campusadda.vendorops.order.dto.response.*;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.entity.OrderStatusHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .vendorId(order.getVendor().getId())
                .orderNumber(order.getOrderNumber())
                .sourceSystem(order.getSourceSystem())
                .externalOrderId(order.getExternalOrderId())
                .externalCustomerId(order.getExternalCustomerId())
                .orderSource(order.getOrderSource())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .pickupEtaAt(order.getPickupEtaAt())
                .placedAt(order.getPlacedAt())
                .acceptedAt(order.getAcceptedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemNameSnapshot(item.getItemNameSnapshot())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .specialInstructions(item.getSpecialInstructions())
                .recipeSnapshotJson(item.getRecipeSnapshotJson())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public OrderStatusHistoryResponse toStatusHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .id(history.getId())
                .orderId(history.getOrder().getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedByUserId(history.getChangedByUser() != null ? history.getChangedByUser().getId() : null)
                .remarks(history.getRemarks())
                .changedAt(history.getChangedAt())
                .build();
    }

    public OrderDetailResponse toDetailResponse(
            Order order,
            List<OrderItem> items,
            List<OrderStatusHistory> history
    ) {
        return OrderDetailResponse.builder()
                .order(toResponse(order))
                .items(items.stream().map(this::toItemResponse).toList())
                .statusHistory(history.stream().map(this::toStatusHistoryResponse).toList())
                .build();
    }
}