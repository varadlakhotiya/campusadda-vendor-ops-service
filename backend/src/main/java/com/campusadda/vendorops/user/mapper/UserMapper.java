package com.campusadda.vendorops.user.mapper;

import java.util.List;

import com.campusadda.vendorops.user.dto.response.RoleResponse;
import com.campusadda.vendorops.user.dto.response.UserDetailResponse;
import com.campusadda.vendorops.user.dto.response.UserResponse;
import com.campusadda.vendorops.user.entity.User;
import com.campusadda.vendorops.vendor.entity.VendorUserAssignment;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserDetailResponse toDetailResponse(
            User user,
            List<RoleResponse> roles,
            List<VendorUserAssignment> assignments) {

        List<UserDetailResponse.VendorAssignmentInfo> vendorInfos = assignments == null
                ? List.of()
                : assignments.stream()
                .map(this::mapVendorAssignment)
                .toList();

        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .vendorAssignments(vendorInfos)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserDetailResponse.VendorAssignmentInfo mapVendorAssignment(VendorUserAssignment assignment) {
        return UserDetailResponse.VendorAssignmentInfo.builder()
                .vendorId(assignment.getVendor().getId())
                .vendorCode(assignment.getVendor().getVendorCode())
                .vendorName(assignment.getVendor().getName())
                .assignmentRole(assignment.getAssignmentRole())
                .isPrimary(assignment.getIsPrimary())
                .status(assignment.getStatus())
                .build();
    }
}