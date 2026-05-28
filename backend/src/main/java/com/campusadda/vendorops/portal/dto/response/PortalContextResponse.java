package com.campusadda.vendorops.portal.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortalContextResponse {

    private Long currentUserId;
    private String fullName;
    private String email;
    private List<String> roles;
    private boolean adminUser;
    private boolean vendorUser;
    private Long primaryVendorId;
    private String primaryVendorName;
    private List<VendorInfo> assignedVendors;

    @Getter
    @Builder
    public static class VendorInfo {
        private Long vendorId;
        private String vendorName;
        private String assignmentRole;
    }
}