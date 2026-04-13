package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PublicVendorMenuResponse {
    private Long vendorId;
    private String vendorName;
    private String locationLabel;
    private String campusArea;
    private List<PublicMenuCategoryResponse> categories;
    private List<PublicMenuItemResponse> items;
}