package com.campusadda.vendorops.vendor.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorResponse {

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}