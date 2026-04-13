package com.campusadda.vendorops.security.impl;

import com.campusadda.vendorops.common.exception.ForbiddenException;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorAccessServiceImpl implements VendorAccessService {

    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final SecurityUtils securityUtils;

    @Override
    public void validateVendorAccess(Long vendorId) {
        validateVendorViewAccess(vendorId);
    }

    @Override
    public void validateVendorViewAccess(Long vendorId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        if (currentUserId == null) {
            throw new ForbiddenException("Unauthenticated access");
        }

        if (securityUtils.hasRole("ADMIN")) {
            return;
        }

        boolean assigned = vendorUserAssignmentRepository
                .existsByVendor_IdAndUser_Id(vendorId, currentUserId);

        if (!assigned) {
            throw new ForbiddenException("You are not allowed to access this vendor");
        }
    }

    @Override
    public void validateVendorManageAccess(Long vendorId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        if (currentUserId == null) {
            throw new ForbiddenException("Unauthenticated access");
        }

        if (securityUtils.hasRole("ADMIN")) {
            throw new ForbiddenException("Admin can view vendor orders but cannot manage vendor workflow");
        }

        boolean vendorUser =
                securityUtils.hasRole("VENDOR_MANAGER") || securityUtils.hasRole("VENDOR_STAFF");

        if (!vendorUser) {
            throw new ForbiddenException("Only vendor users can manage vendor workflow");
        }

        boolean assigned = vendorUserAssignmentRepository
                .existsByVendor_IdAndUser_Id(vendorId, currentUserId);

        if (!assigned) {
            throw new ForbiddenException("You are not allowed to manage this vendor");
        }
    }
}