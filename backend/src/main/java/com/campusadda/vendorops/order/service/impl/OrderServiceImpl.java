package com.campusadda.vendorops.order.service.impl;

import com.campusadda.vendorops.common.enums.OrderStatus;
import com.campusadda.vendorops.common.enums.PaymentStatus;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.order.dto.request.CreateManualOrderRequest;
import com.campusadda.vendorops.order.dto.request.CreateOrderItemRequest;
import com.campusadda.vendorops.order.dto.request.CreateOrderRequest;
import com.campusadda.vendorops.order.dto.response.OrderBoardResponse;
import com.campusadda.vendorops.order.dto.response.OrderDetailResponse;
import com.campusadda.vendorops.order.dto.response.OrderResponse;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.entity.OrderStatusHistory;
import com.campusadda.vendorops.order.mapper.OrderMapper;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.order.repository.OrderStatusHistoryRepository;
import com.campusadda.vendorops.order.service.OrderPricingService;
import com.campusadda.vendorops.order.service.OrderService;
import com.campusadda.vendorops.order.service.OrderValidationService;
import com.campusadda.vendorops.outbox.service.OutboxService; // ✅ NEW IMPORT
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderValidationService orderValidationService;
    private final OrderPricingService orderPricingService;
    private final OrderMapper orderMapper;
    private final VendorValidator vendorValidator;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    private final OutboxService outboxService; // ✅ NEW DEPENDENCY

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(request.getVendorId());
        Map<Long, MenuItem> menuItemsById = orderValidationService.validateAndLoadMenuItems(request);

        BigDecimal subtotal = orderPricingService.calculateSubtotal(request.getItems(), menuItemsById);
        BigDecimal discount = orderPricingService.calculateDiscount(subtotal);
        BigDecimal tax = orderPricingService.calculateTax(subtotal, discount);
        BigDecimal total = orderPricingService.calculateTotal(subtotal, discount, tax);

        User currentUser = resolveCurrentUser();

        Order order = new Order();
        order.setVendor(vendor);
        order.setOrderNumber(generateOrderNumber());
        order.setSourceSystem(request.getSourceSystem() != null ? request.getSourceSystem() : "VENDOR_OPS");
        order.setExternalOrderId(request.getExternalOrderId());
        order.setExternalCustomerId(request.getExternalCustomerId());
        order.setOrderSource(request.getOrderSource() != null ? request.getOrderSource() : "APP");
        order.setStatus(OrderStatus.CREATED.name());
        order.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : PaymentStatus.PENDING.name());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setTaxAmount(tax);
        order.setTotalAmount(total);
        order.setNotes(request.getNotes());
        order.setPlacedAt(LocalDateTime.now());
        order.setCreatedByUser(currentUser);

        Order savedOrder = orderRepository.save(order);

        // ✅ OUTBOX EVENT (IMPORTANT ADDITION)
        outboxService.saveEvent(
                "ORDER",
                savedOrder.getId(),
                "ORDER_CREATED",
                savedOrder.getOrderNumber(),
                "{\"orderId\":" + savedOrder.getId() +
                        ",\"orderNumber\":\"" + savedOrder.getOrderNumber() + "\"}"
        );

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemsById.get(itemRequest.getMenuItemId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemNameSnapshot(menuItem.getItemName());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setLineTotal(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            orderItem.setRecipeSnapshotJson(null);

            orderItemRepository.save(orderItem);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(savedOrder);
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.CREATED.name());
        history.setChangedByUser(currentUser);
        history.setChangedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse createManualOrder(Long vendorId, CreateManualOrderRequest request) {
        request.setVendorId(vendorId);
        if (request.getOrderSource() == null) {
            request.setOrderSource("MANUAL");
        }
        return createOrder(request);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrder_IdOrderByChangedAtAsc(orderId);

        return orderMapper.toDetailResponse(order, items, history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);

        return orderRepository.findByVendor_IdOrderByPlacedAtDesc(vendorId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderBoardResponse getOrderBoard(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);

        return OrderBoardResponse.builder()
                .created(mapList(vendorId, OrderStatus.CREATED.name()))
                .accepted(mapList(vendorId, OrderStatus.ACCEPTED.name()))
                .preparing(mapList(vendorId, OrderStatus.PREPARING.name()))
                .ready(mapList(vendorId, OrderStatus.READY.name()))
                .completed(mapList(vendorId, OrderStatus.COMPLETED.name()))
                .build();
    }

    private List<OrderResponse> mapList(Long vendorId, String status) {
        return orderRepository.findByVendor_IdAndStatusOrderByPlacedAtDesc(vendorId, status)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
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