package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicVendorResponse {
    private Long id;
    private String vendorCode;
    private String name;
    private String description;
    private String locationLabel;
    private String campusArea;
}