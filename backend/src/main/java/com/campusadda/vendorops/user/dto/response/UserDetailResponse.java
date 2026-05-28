package com.campusadda.vendorops.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime lastLoginAt;
    private List<RoleResponse> roles;
    private List<VendorAssignmentInfo> vendorAssignments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class VendorAssignmentInfo {
        private Long vendorId;
        private String vendorCode;
        private String vendorName;
        private String assignmentRole;
        private Boolean isPrimary;
        private String status;
    }
}