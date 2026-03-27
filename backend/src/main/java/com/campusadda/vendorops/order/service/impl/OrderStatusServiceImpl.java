package com.campusadda.vendorops.order.service.impl;

import com.campusadda.vendorops.common.enums.OrderStatus;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.order.dto.request.CancelOrderRequest;
import com.campusadda.vendorops.order.dto.request.UpdateOrderStatusRequest;
import com.campusadda.vendorops.order.dto.response.OrderResponse;
import com.campusadda.vendorops.order.dto.response.OrderStatusHistoryResponse;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderStatusHistory;
import com.campusadda.vendorops.order.mapper.OrderMapper;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.order.repository.OrderStatusHistoryRepository;
import com.campusadda.vendorops.order.service.OrderStatusService;
import com.campusadda.vendorops.order.service.OrderStockConsumptionService;
import com.campusadda.vendorops.order.validator.OrderStatusTransitionValidator;
import com.campusadda.vendorops.order.validator.OrderValidator;
import com.campusadda.vendorops.outbox.service.OutboxService; // ✅ NEW
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderValidator orderValidator;
    private final OrderStatusTransitionValidator transitionValidator;
    private final OrderStockConsumptionService stockConsumptionService;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    private final OutboxService outboxService; // ✅ NEW

    @Override
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderValidator.validateOrderExists(orderId);

        String fromStatus = order.getStatus();
        String toStatus = request.getStatus();

        transitionValidator.validateTransition(fromStatus, toStatus);

        if (OrderStatus.ACCEPTED.name().equals(toStatus)) {
            stockConsumptionService.consumeStock(order);
            order.setAcceptedAt(LocalDateTime.now());
        } else if (OrderStatus.PREPARING.name().equals(toStatus)) {
            order.setPreparingAt(LocalDateTime.now());
        } else if (OrderStatus.READY.name().equals(toStatus)) {
            order.setReadyAt(LocalDateTime.now());
        } else if (OrderStatus.COMPLETED.name().equals(toStatus)) {
            order.setCompletedAt(LocalDateTime.now());
        }

        order.setStatus(toStatus);
        Order saved = orderRepository.save(order);

        // ✅ OUTBOX EVENT ADDED HERE
        outboxService.saveEvent(
                "ORDER",
                saved.getId(),
                "ORDER_STATUS_UPDATED",
                saved.getOrderNumber(),
                "{\"orderId\":" + saved.getId() +
                        ",\"status\":\"" + saved.getStatus() + "\"}"
        );

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(saved);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setRemarks(request.getRemarks());
        history.setChangedByUser(resolveCurrentUser());
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        Order order = orderValidator.validateOrderExists(orderId);

        String fromStatus = order.getStatus();
        transitionValidator.validateTransition(fromStatus, OrderStatus.CANCELLED.name());

        if (OrderStatus.ACCEPTED.name().equals(fromStatus) || OrderStatus.PREPARING.name().equals(fromStatus)) {
            stockConsumptionService.reverseStock(order);
        }

        order.setStatus(OrderStatus.CANCELLED.name());
        order.setCancelledAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(saved);
        history.setFromStatus(fromStatus);
        history.setToStatus(OrderStatus.CANCELLED.name());
        history.setRemarks(request.getReason());
        history.setChangedByUser(resolveCurrentUser());
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getStatusHistory(Long orderId) {
        orderValidator.validateOrderExists(orderId);

        return statusHistoryRepository.findByOrder_IdOrderByChangedAtAsc(orderId)
                .stream()
                .map(orderMapper::toStatusHistoryResponse)
                .toList();
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