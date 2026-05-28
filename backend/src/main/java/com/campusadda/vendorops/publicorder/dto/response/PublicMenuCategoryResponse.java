package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicMenuCategoryResponse {
    private Long id;
    private String categoryName;
    private Integer displayOrder;
}