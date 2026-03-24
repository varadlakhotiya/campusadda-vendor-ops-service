package com.campusadda.vendorops.vendor.service.impl;

import java.util.List;

import com.campusadda.vendorops.vendor.dto.request.AssignVendorUserRequest;
import com.campusadda.vendorops.vendor.dto.response.VendorUserAssignmentResponse;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import com.campusadda.vendorops.vendor.repository.VendorUserAssignmentRepository;
import com.campusadda.vendorops.vendor.service.VendorUserAssignmentService;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import com.campusadda.vendorops.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorUserAssignmentServiceImpl implements VendorUserAssignmentService {

    private final VendorUserAssignmentRepository vendorUserAssignmentRepository;
    private final VendorValidator vendorValidator;

    @Override
    public VendorUserAssignmentResponse assignUserToVendor(Long vendorId, AssignVendorUserRequest request) {
        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        User user = vendorValidator.validateUserExists(request.getUserId());
        vendorValidator.validateVendorUserNotAlreadyAssigned(vendorId, user.getId());

        VendorUserAssignment assignment = new VendorUserAssignment();
        assignment.setVendor(vendor);
        assignment.setUser(user);
        assignment.setAssignmentRole(request.getAssignmentRole());
        assignment.setIsPrimary(request.getIsPrimary());
        assignment.setStatus("ACTIVE");

        VendorUserAssignment savedAssignment = vendorUserAssignmentRepository.save(assignment);
        return mapResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorUserAssignmentResponse> getVendorUsers(Long vendorId) {
        vendorValidator.validateVendorExists(vendorId);

        return vendorUserAssignmentRepository.findByVendor_Id(vendorId)
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorUserAssignmentResponse> getUserVendorAssignments(Long userId) {
        vendorValidator.validateUserExists(userId);

        return vendorUserAssignmentRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public void removeUserFromVendor(Long vendorId, Long userId) {
        vendorValidator.validateVendorExists(vendorId);
        vendorValidator.validateUserExists(userId);
        vendorValidator.validateAssignmentExists(vendorId, userId);

        vendorUserAssignmentRepository.deleteByVendor_IdAndUser_Id(vendorId, userId);
    }

    private VendorUserAssignmentResponse mapResponse(VendorUserAssignment assignment) {
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