package com.campusadda.vendorops.onboarding.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VendorSignupRequestResponse {
    private Long id;
    private String restaurantName;
    private String contactPersonName;
    private String contactEmail;
    private String contactPhone;
    private String campusArea;
    private String locationLabel;
    private String requestedRoleCode;
    private String status;
    private String rejectionReason;
    private Long createdVendorId;
    private Long createdUserId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}