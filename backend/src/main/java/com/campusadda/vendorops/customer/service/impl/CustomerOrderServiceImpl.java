package com.campusadda.vendorops.customer.service.impl;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderDetailResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderHistoryItemResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderHistoryResponse;
import com.campusadda.vendorops.customer.dto.response.CustomerOrderTimelineResponse;
import com.campusadda.vendorops.customer.service.CustomerOrderService;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.entity.OrderStatusHistory;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.order.repository.OrderStatusHistoryRepository;
import com.campusadda.vendorops.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("CREATED", "ACCEPTED", "PREPARING", "READY");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final SecurityUtils securityUtils;

    @Override
    public List<CustomerOrderHistoryResponse> getMyOrders() {
        String customerId = getCurrentCustomerId();

        return orderRepository.findByExternalCustomerIdOrderByPlacedAtDesc(customerId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public List<CustomerOrderHistoryResponse> getMyActiveOrders() {
        String customerId = getCurrentCustomerId();

        return orderRepository.findByExternalCustomerIdOrderByPlacedAtDesc(customerId)
                .stream()
                .filter(order -> ACTIVE_STATUSES.contains(String.valueOf(order.getStatus()).toUpperCase()))
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public CustomerOrderDetailResponse getMyOrder(Long orderId) {
        String customerId = getCurrentCustomerId();

        Order order = orderRepository.findByIdAndExternalCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for current customer"));

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrder_IdOrderByChangedAtAsc(orderId);

        return CustomerOrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .vendorId(order.getVendor().getId())
                .vendorName(order.getVendor().getName())
                .status(order.getStatus())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .notes(order.getNotes())
                .totalAmount(order.getTotalAmount())
                .placedAt(order.getPlacedAt())
                .acceptedAt(order.getAcceptedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .items(items.stream().map(this::toItemResponse).toList())
                .statusHistory(history.stream()
                        .map(entry -> CustomerOrderTimelineResponse.builder()
                                .id(entry.getId())
                                .fromStatus(entry.getFromStatus())
                                .toStatus(entry.getToStatus())
                                .remarks(entry.getRemarks())
                                .changedAt(entry.getChangedAt())
                                .build())
                        .toList())
                .build();
    }

    private CustomerOrderHistoryResponse toHistoryResponse(Order order) {
        List<CustomerOrderHistoryItemResponse> items = orderItemRepository.findByOrder_Id(order.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();

        return CustomerOrderHistoryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .vendorId(order.getVendor().getId())
                .vendorName(order.getVendor().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .placedAt(order.getPlacedAt())
                .items(items)
                .build();
    }

    private CustomerOrderHistoryItemResponse toItemResponse(OrderItem item) {
        return CustomerOrderHistoryItemResponse.builder()
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemName(item.getItemNameSnapshot())
                .quantity(item.getQuantity())
                .build();
    }

    private String getCurrentCustomerId() {
        return String.valueOf(securityUtils.getCurrentUserId());
    }
}