package com.campusadda.vendorops.menu.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMenuCategoryRequest {
    private String categoryName;
    private Integer displayOrder;
    private Boolean isActive;
    private String externalCategoryId;
}