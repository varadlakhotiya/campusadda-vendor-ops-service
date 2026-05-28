package com.campusadda.vendorops.portal.service.impl;

import com.campusadda.vendorops.portal.dto.response.PortalContextResponse;
import com.campusadda.vendorops.portal.service.PortalContextService;
import com.campusadda.vendorops.security.SecurityUtils;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.user.entity.UserRole;
import com.campusadda.vendorops.user.repository.UserRepository;
import com.campusadda.vendorops.user.repository.UserRoleRepository;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalContextServiceImpl implements PortalContextService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final SecurityUtils securityUtils;

    @Override
    public PortalContextResponse getCurrentContext() {
        Long currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = userRoleRepository.findByUser_Id(currentUserId)    
                .stream()
                .map(UserRole::getRole)
                .map(role -> role.getRoleCode())
                .toList();
        var assignments = vendorUserAssignmentRepository.findByUser_Id(currentUserId);
        var assignedVendors = assignments.stream()
                .map(a -> PortalContextResponse.VendorInfo.builder()
                        .vendorId(a.getVendor().getId())
                        .vendorName(a.getVendor().getName())
                        .assignmentRole(a.getAssignmentRole())
                        .build())
                        .toList();

        boolean adminUser = roles.contains("ADMIN");    
        boolean vendorUser = roles.contains("VENDOR_MANAGER") || roles.contains("VENDOR_STAFF");

        var primaryAssignment = assignments.stream()    
                .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                .findFirst() 
                .orElse(assignments.isEmpty() ? null : assignments.get(0));

        Long primaryVendorId = primaryAssignment == null ? null : primaryAssignment.getVendor().getId();    
        String primaryVendorName = primaryAssignment == null ? null : primaryAssignment.getVendor().getName();

        return PortalContextResponse.builder()
                .currentUserId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .adminUser(adminUser)
                .vendorUser(vendorUser)
                .primaryVendorId(primaryVendorId)
                .primaryVendorName(primaryVendorName)
                .assignedVendors(assignedVendors)
                .build();
        }
}