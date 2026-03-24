package com.campusadda.vendorops.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMenuCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String categoryName;
    private Integer displayOrder;
    private Boolean isActive;
    private String sourceSystem;
    private String externalCategoryId;
}