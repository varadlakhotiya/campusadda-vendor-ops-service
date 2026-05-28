package com.campusadda.vendorops.vendor.service.impl;

import java.time.LocalDate;
import java.util.List;

import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.vendor.dto.request.CreateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorRequest;
import com.campusadda.vendorops.vendor.dto.request.UpdateVendorStatusRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorDetailResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorSummaryResponse;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.mapper.VendorMapper;
import com.campusadda.vendorops.vendor.repository.VendorRepository;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import com.campusadda.vendorops.vendor.service.VendorService;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final VendorValidator vendorValidator;
    private final VendorMapper vendorMapper;
    private final VendorAccessService vendorAccessService;
    private final EntityManager entityManager;

    @Override
    public VendorResponse createVendor(CreateVendorRequest request) {
        vendorValidator.validateVendorCodeUnique(request.getVendorCode());

        Vendor vendor = vendorMapper.toEntity(request);
        Vendor savedVendor = vendorRepository.save(vendor);

        return vendorMapper.toResponse(savedVendor);
    }

    @Override
    public VendorResponse updateVendor(Long vendorId, UpdateVendorRequest request) {
        vendorValidator.validateVendorExists(vendorId);
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        vendorMapper.updateEntity(vendor, request);

        Vendor updatedVendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(updatedVendor);
    }

    @Override
    public VendorResponse updateVendorStatus(Long vendorId, UpdateVendorStatusRequest request) {
        vendorValidator.validateVendorExists(vendorId);
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        vendor.setStatus(request.getStatus());

        Vendor updatedVendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(updatedVendor);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDetailResponse getVendorById(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);

        List<VendorUserAssignmentResponse> assignedUsers = vendorUserAssignmentRepository.findByVendor_Id(vendorId)
                .stream()
                .map(this::mapAssignmentResponse)
                .toList();

        return vendorMapper.toDetailResponse(vendor, assignedUsers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorResponse> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(vendorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorSummaryResponse getVendorSummary(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);

        long assignedUserCount = vendorUserAssignmentRepository.findByVendor_Id(vendorId).size();
        long activeMenuItemCount = countActiveMenuItems(vendorId);
        long lowStockItemCount = countLowStockItems(vendorId);
        long todayOrderCount = countTodayOrders(vendorId);

        return VendorSummaryResponse.builder()
                .vendorId(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getName())
                .status(vendor.getStatus() == null ? null : String.valueOf(vendor.getStatus()))
                .assignedUserCount(assignedUserCount)
                .activeMenuItemCount(activeMenuItemCount)
                .lowStockItemCount(lowStockItemCount)
                .todayOrderCount(todayOrderCount)
                .todayRevenue(0.0)
                .build();
    }

    private long countActiveMenuItems(Long vendorId) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM menu_items
                WHERE vendor_id = :vendorId
                  AND COALESCE(is_active, 0) = 1
                """)
                .setParameter("vendorId", vendorId)
                .getSingleResult();

        return result == null ? 0L : result.longValue();
    }

    private long countLowStockItems(Long vendorId) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM inventory_items
                WHERE vendor_id = :vendorId
                  AND current_quantity IS NOT NULL
                  AND low_stock_threshold IS NOT NULL
                  AND current_quantity <= low_stock_threshold
                """)
                .setParameter("vendorId", vendorId)
                .getSingleResult();

        return result == null ? 0L : result.longValue();
    }

    private long countTodayOrders(Long vendorId) {
        LocalDate today = LocalDate.now();

        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM orders
                WHERE vendor_id = :vendorId
                  AND created_at >= :startOfDay
                  AND created_at < :startOfNextDay
                """)
                .setParameter("vendorId", vendorId)
                .setParameter("startOfDay", today.atStartOfDay())
                .setParameter("startOfNextDay", today.plusDays(1).atStartOfDay())
                .getSingleResult();

        return result == null ? 0L : result.longValue();
    }

    private VendorUserAssignmentResponse mapAssignmentResponse(VendorUserAssignment assignment) {
        return VendorUserAssignmentResponse.builder()
                .id(assignment.getId())
                .vendorId(assignment.getVendor().getId())
                .vendorCode(assignment.getVendor().getVendorCode())
                .vendorName(assignment.getVendor().getName())
                .userId(assignment.getUser().getId())
                .userFullName(assignment.getUser().getFullName())
                .userEmail(assignment.getUser().getEmail())
                .assignmentRole(assignment.getAssignmentRole())
                .isPrimary(assignment.getIsPrimary())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}