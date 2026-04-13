package com.campusadda.vendorops.inventory.service.impl;

import com.campusadda.vendorops.common.enums.MovementType;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.inventory.dto.request.CreateInventoryItemRequest;
import com.campusadda.vendorops.inventory.dto.request.StockAdjustmentRequest;
import com.campusadda.vendorops.inventory.dto.request.StockInRequest;
import com.campusadda.vendorops.inventory.dto.request.StockOutRequest;
import com.campusadda.vendorops.inventory.dto.request.UpdateInventoryItemRequest;
import com.campusadda.vendorops.inventory.dto.request.UpdateInventoryItemStatusRequest;
import com.campusadda.vendorops.inventory.dto.response.InventoryItemResponse;
import com.campusadda.vendorops.inventory.dto.response.LowStockItemResponse;
import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.entity.StockMovement;
import com.campusadda.vendorops.inventory.mapper.InventoryMapper;
import com.campusadda.vendorops.inventory.repository.InventoryItemRepository;
import com.campusadda.vendorops.inventory.repository.StockMovementRepository;
import com.campusadda.vendorops.inventory.service.InventoryService;
import com.campusadda.vendorops.inventory.validator.InventoryValidator;
import com.campusadda.vendorops.inventory.validator.StockOperationValidator;
import com.campusadda.vendorops.outbox.service.OutboxService;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.VendorAccessService;
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

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryValidator inventoryValidator;
    private final StockOperationValidator stockOperationValidator;
    private final InventoryMapper inventoryMapper;
    private final VendorValidator vendorValidator;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final VendorAccessService vendorAccessService;

    @Override
    public InventoryItemResponse createInventoryItem(Long vendorId, CreateInventoryItemRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        inventoryValidator.validateUniqueItemCode(vendorId, request.getItemCode());

        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getCurrentQuantity(), "Current quantity");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getReservedQuantity(), "Reserved quantity");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getLowStockThreshold(), "Low stock threshold");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getMaxStockLevel(), "Max stock level");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getUnitCost(), "Unit cost");

        InventoryItem item = inventoryMapper.toEntity(request);
        item.setVendor(vendor);

        if (item.getCurrentQuantity() == null) item.setCurrentQuantity(BigDecimal.ZERO);
        if (item.getReservedQuantity() == null) item.setReservedQuantity(BigDecimal.ZERO);
        if (item.getLowStockThreshold() == null) item.setLowStockThreshold(BigDecimal.ZERO);
        if (item.getStatus() == null || item.getStatus().isBlank()) item.setStatus("ACTIVE");
        if (item.getSourceSystem() == null || item.getSourceSystem().isBlank()) item.setSourceSystem("VENDOR_OPS");

        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryItems(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId);

        vendorValidator.validateVendorExists(vendorId);
        return inventoryItemRepository.findByVendor_IdOrderByItemNameAsc(vendorId)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItemById(Long vendorId, Long inventoryItemId) {
        vendorAccessService.validateVendorAccess(vendorId);

        return inventoryMapper.toResponse(
                inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId)
        );
    }

    @Override
    public InventoryItemResponse updateInventoryItem(Long vendorId, Long inventoryItemId, UpdateInventoryItemRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getReservedQuantity(), "Reserved quantity");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getLowStockThreshold(), "Low stock threshold");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getMaxStockLevel(), "Max stock level");
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getUnitCost(), "Unit cost");

        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        inventoryMapper.updateEntity(item, request);
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    public InventoryItemResponse updateInventoryItemStatus(Long vendorId, Long inventoryItemId, UpdateInventoryItemStatusRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        item.setStatus(request.getStatus());
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    public InventoryItemResponse stockIn(Long vendorId, Long inventoryItemId, StockInRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        stockOperationValidator.validatePositiveQuantity(request.getQuantity());
        stockOperationValidator.validateOptionalZeroOrPositiveQuantity(request.getUnitCost(), "Unit cost");

        BigDecimal before = safe(item.getCurrentQuantity());
        BigDecimal after = before.add(request.getQuantity());

        item.setCurrentQuantity(after);
        item.setLastRestockedAt(LocalDateTime.now());

        if (request.getUnitCost() != null) {
            item.setUnitCost(request.getUnitCost());
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.IN.name(), request.getQuantity(), before, after, request.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    public InventoryItemResponse stockOut(Long vendorId, Long inventoryItemId, StockOutRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);
        stockOperationValidator.validatePositiveQuantity(request.getQuantity());

        BigDecimal before = safe(item.getCurrentQuantity());
        stockOperationValidator.validateStockOutAllowed(before, request.getQuantity());

        BigDecimal after = before.subtract(request.getQuantity());

        item.setCurrentQuantity(after);
        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.OUT.name(), request.getQuantity().negate(), before, after, saved.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    public InventoryItemResponse adjustStock(Long vendorId, Long inventoryItemId, StockAdjustmentRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        stockOperationValidator.validateZeroOrPositiveQuantity(request.getAdjustedQuantity(), "Adjusted quantity");

        InventoryItem item = inventoryValidator.validateInventoryItemExists(vendorId, inventoryItemId);

        BigDecimal before = safe(item.getCurrentQuantity());
        BigDecimal after = request.getAdjustedQuantity();
        BigDecimal delta = after.subtract(before);

        item.setCurrentQuantity(after);
        InventoryItem saved = inventoryItemRepository.save(item);
        saveMovement(saved, MovementType.ADJUSTMENT.name(), delta, before, after, saved.getUnitCost(), request.getReason());

        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockItemResponse> getLowStockItems(Long vendorId) {
        vendorAccessService.validateVendorAccess(vendorId);

        vendorValidator.validateVendorExists(vendorId);

        return inventoryItemRepository.findByVendor_IdOrderByItemNameAsc(vendorId)
                .stream()
                .filter(item -> safe(item.getCurrentQuantity()).compareTo(safe(item.getLowStockThreshold())) <= 0)
                .map(inventoryMapper::toLowStockResponse)
                .toList();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void saveMovement(
            InventoryItem item,
            String movementType,
            BigDecimal delta,
            BigDecimal before,
            BigDecimal after,
            BigDecimal unitCost,
            String reason) {

        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        StockMovement movement = new StockMovement();
        movement.setVendor(item.getVendor());
        movement.setInventoryItem(item);
        movement.setMovementType(movementType);
        movement.setQuantityDelta(delta);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);
        movement.setUnitCost(unitCost);
        movement.setReason(reason);
        movement.setCreatedByUser(currentUser);
        movement.setEventTime(LocalDateTime.now());

        stockMovementRepository.save(movement);

        outboxService.saveEvent(
                "INVENTORY",
                item.getId(),
                "STOCK_UPDATED",
                item.getItemCode(),
                "{\"inventoryItemId\":" + item.getId() +
                        ",\"currentQuantity\":\"" + item.getCurrentQuantity() + "\"}"
        );
    }
}