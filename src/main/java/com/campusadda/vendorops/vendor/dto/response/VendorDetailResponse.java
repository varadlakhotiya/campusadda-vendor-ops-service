package com.campusadda.vendorops.vendor.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorDetailResponse {

    private Long id;
    private String vendorCode;
    private String name;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String locationLabel;
    private String campusArea;
    private String status;
    private String sourceSystem;
    private String externalVendorId;
    private List<VendorUserAssignmentResponse> assignedUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}