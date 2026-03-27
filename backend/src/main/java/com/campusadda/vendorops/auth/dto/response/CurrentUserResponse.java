package com.campusadda.vendorops.auth.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;
    private List<AssignedVendorInfo> assignedVendors;

    @Getter
    @Builder
    public static class AssignedVendorInfo {
        private Long vendorId;
        private String vendorCode;
        private String vendorName;
        private String assignmentRole;
        private Boolean isPrimary;
    }
}