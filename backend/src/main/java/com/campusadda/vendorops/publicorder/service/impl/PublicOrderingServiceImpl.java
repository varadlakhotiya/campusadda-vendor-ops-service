package com.campusadda.vendorops.publicorder.service.impl;

import com.campusadda.vendorops.common.exception.BusinessException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuCategory;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.repository.MenuCategoryRepository;
import com.campusadda.vendorops.menu.repository.MenuItemRepository;
import com.campusadda.vendorops.order.entity.Order;
import com.campusadda.vendorops.order.entity.OrderItem;
import com.campusadda.vendorops.order.entity.OrderStatusHistory;
import com.campusadda.vendorops.order.repository.OrderItemRepository;
import com.campusadda.vendorops.order.repository.OrderRepository;
import com.campusadda.vendorops.order.repository.OrderStatusHistoryRepository;
import com.campusadda.vendorops.publicorder.dto.request.CustomerCreateOrderRequest;
import com.campusadda.vendorops.publicorder.dto.request.CustomerOrderItemRequest;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderSummaryItemResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderSummaryResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderTrackingHistoryResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderTrackingItemResponse;
import com.campusadda.vendorops.publicorder.dto.response.CustomerOrderTrackingResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicMenuCategoryResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicMenuItemResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorMenuResponse;
import com.campusadda.vendorops.publicorder.dto.response.PublicVendorResponse;
import com.campusadda.vendorops.publicorder.service.PublicOrderingService;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.UserPrincipal;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicOrderingServiceImpl implements PublicOrderingService {

    private static final Set<String> ACTIVE_ORDER_STATUSES =
            Set.of("CREATED", "ACCEPTED", "PREPARING", "READY");

    private final VendorRepository vendorRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PublicVendorResponse> getActiveVendors() {
        return vendorRepository.findByStatusOrderByNameAsc("ACTIVE")
                .stream()
                .map(vendor -> PublicVendorResponse.builder()
                        .id(vendor.getId())
                        .vendorCode(vendor.getVendorCode())
                        .name(vendor.getName())
                        .description(vendor.getDescription())
                        .locationLabel(vendor.getLocationLabel())
                        .campusArea(vendor.getCampusArea())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicVendorMenuResponse getVendorMenu(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (!"ACTIVE".equalsIgnoreCase(vendor.getStatus())) {
            throw new BusinessException("Vendor is not active");
        }

        List<MenuCategory> categories =
                menuCategoryRepository.findByVendor_IdAndIsActiveTrueOrderByDisplayOrderAsc(vendorId);

        List<MenuItem> items =
                menuItemRepository.findByVendor_IdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(vendorId);

        return PublicVendorMenuResponse.builder()
                .vendorId(vendor.getId())
                .vendorName(vendor.getName())
                .locationLabel(vendor.getLocationLabel())
                .campusArea(vendor.getCampusArea())
                .categories(categories.stream()
                        .map(category -> PublicMenuCategoryResponse.builder()
                                .id(category.getId())
                                .categoryName(category.getCategoryName())
                                .displayOrder(category.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .items(items.stream()
                        .map(item -> PublicMenuItemResponse.builder()
                                .id(item.getId())
                                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                                .itemCode(item.getItemCode())
                                .itemName(item.getItemName())
                                .description(item.getDescription())
                                .price(item.getPrice())
                                .prepTimeMinutes(item.getPrepTimeMinutes() != null
                                        ? item.getPrepTimeMinutes().intValue()
                                        : null)
                                .isVeg(item.getIsVeg())
                                .primaryImageUrl(item.getPrimaryImageUrl())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public CustomerOrderResponse placeOrder(CustomerCreateOrderRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (!"ACTIVE".equalsIgnoreCase(vendor.getStatus())) {
            throw new BusinessException("Vendor is not active");
        }

        List<Long> menuItemIds = request.getItems().stream()
                .map(CustomerOrderItemRequest::getMenuItemId)
                .distinct()
                .collect(Collectors.toList());

        List<MenuItem> menuItems =
                menuItemRepository.findByVendor_IdAndIdInAndIsActiveTrueAndIsAvailableTrue(
                        vendor.getId(),
                        menuItemIds
                );

        if (menuItems.size() != menuItemIds.size()) {
            throw new BusinessException("One or more menu items are invalid or unavailable");
        }

        Map<Long, MenuItem> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CustomerOrderItemRequest item : request.getItems()) {
            MenuItem menuItem = menuItemMap.get(item.getMenuItemId());
            if (menuItem == null) {
                throw new BusinessException("Invalid menu item selected: " + item.getMenuItemId());
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Quantity must be greater than zero");
            }

            subtotal = subtotal.add(
                    menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        Order order = new Order();
        order.setVendor(vendor);
        order.setOrderNumber(generateOrderNumber());
        order.setSourceSystem("CUSTOMER_PORTAL");
        order.setOrderSource("WEB");
        order.setStatus("CREATED");
        order.setPaymentStatus("PENDING");
        order.setExternalCustomerId(resolveAuthenticatedCustomerId());
        order.setCustomerName(safeTrim(request.getCustomerName()));
        order.setCustomerPhone(normalizePhone(request.getCustomerPhone()));
        order.setNotes(safeTrim(request.getNotes()));
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setPlacedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    MenuItem menuItem = menuItemMap.get(itemRequest.getMenuItemId());

                    OrderItem item = new OrderItem();
                    item.setOrder(savedOrder);
                    item.setMenuItem(menuItem);
                    item.setItemNameSnapshot(menuItem.getItemName());
                    item.setUnitPrice(menuItem.getPrice());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setLineTotal(
                            menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    );
                    item.setSpecialInstructions(safeTrim(itemRequest.getSpecialInstructions()));
                    item.setRecipeSnapshotJson(null);
                    return item;
                })
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(savedOrder);
        history.setFromStatus(null);
        history.setToStatus("CREATED");
        history.setRemarks("Order placed from customer website");
        history.setChangedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);

        return CustomerOrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .vendorId(vendor.getId())
                .vendorName(vendor.getName())
                .status(savedOrder.getStatus())
                .totalAmount(savedOrder.getTotalAmount())
                .placedAt(savedOrder.getPlacedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderTrackingResponse trackOrder(String orderNumber, String customerPhone) {
        String normalizedOrderNumber = normalizeOrderNumber(orderNumber);
        String normalizedInputPhone = normalizePhone(customerPhone);

        if (normalizedOrderNumber.isBlank()) {
            throw new BusinessException("Order number is required");
        }

        if (normalizedInputPhone.isBlank()) {
            throw new BusinessException("Phone number is required");
        }

        Order order = orderRepository.findByOrderNumberIgnoreCase(normalizedOrderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for the provided order number and phone number"
                ));

        String normalizedStoredPhone = normalizePhone(order.getCustomerPhone());
        if (!normalizedInputPhone.equals(normalizedStoredPhone)) {
            throw new ResourceNotFoundException("Order not found for the provided order number and phone number");
        }

        return toTrackingResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrderSummaryResponse> getMyOrders() {
        String externalCustomerId = getCurrentExternalCustomerId();

        return orderRepository.findByExternalCustomerIdOrderByPlacedAtDesc(externalCustomerId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrderSummaryResponse> getMyActiveOrders() {
        String externalCustomerId = getCurrentExternalCustomerId();

        return orderRepository.findByExternalCustomerIdAndStatusInOrderByPlacedAtDesc(
                        externalCustomerId,
                        ACTIVE_ORDER_STATUSES
                )
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderTrackingResponse getMyOrder(Long orderId) {
        String externalCustomerId = getCurrentExternalCustomerId();

        Order order = orderRepository.findByIdAndExternalCustomerId(orderId, externalCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return toTrackingResponse(order);
    }

    private CustomerOrderSummaryResponse toSummaryResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());

        return CustomerOrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .vendorId(order.getVendor().getId())
                .vendorName(order.getVendor().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .placedAt(order.getPlacedAt())
                .items(items.stream()
                        .map(item -> CustomerOrderSummaryItemResponse.builder()
                                .id(item.getId())
                                .itemName(item.getItemNameSnapshot())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }

    private CustomerOrderTrackingResponse toTrackingResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrder_IdOrderByChangedAtAsc(order.getId());

        return CustomerOrderTrackingResponse.builder()
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
                .items(items.stream()
                        .map(item -> CustomerOrderTrackingItemResponse.builder()
                                .id(item.getId())
                                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                                .itemName(item.getItemNameSnapshot())
                                .unitPrice(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .lineTotal(item.getLineTotal())
                                .specialInstructions(item.getSpecialInstructions())
                                .build())
                        .toList())
                .statusHistory(history.stream()
                        .map(entry -> CustomerOrderTrackingHistoryResponse.builder()
                                .id(entry.getId())
                                .fromStatus(entry.getFromStatus())
                                .toStatus(entry.getToStatus())
                                .remarks(entry.getRemarks())
                                .changedAt(entry.getChangedAt())
                                .build())
                        .toList())
                .build();
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

    private String getCurrentExternalCustomerId() {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Authenticated customer not found");
        }
        return String.valueOf(currentUser.getId());
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "WEB-" + timestamp + "-" + suffix;
    }

    private String resolveAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }

        boolean isCustomer = principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CUSTOMER".equalsIgnoreCase(authority.getAuthority()));

        return isCustomer ? String.valueOf(principal.getId()) : null;
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOrderNumber(String orderNumber) {
        return orderNumber == null ? "" : orderNumber.trim().toUpperCase();
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }
}