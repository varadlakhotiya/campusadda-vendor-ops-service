package com.campusadda.vendorops.vendor.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorUserAssignmentResponse {

    private Long id;
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String assignmentRole;
    private Boolean isPrimary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}