package com.campusadda.vendorops.menu.mapper;

import com.campusadda.vendorops.menu.dto.request.CreateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.response.MenuCategoryResponse;
import com.campusadda.vendorops.menu.entity.MenuCategory;
import org.springframework.stereotype.Component;

@Component
public class MenuCategoryMapper {

    public MenuCategory toEntity(CreateMenuCategoryRequest request) {
        MenuCategory category = new MenuCategory();
        category.setCategoryName(request.getCategoryName());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);
        category.setSourceSystem(request.getSourceSystem() != null ? request.getSourceSystem() : "VENDOR_OPS");
        category.setExternalCategoryId(request.getExternalCategoryId());
        return category;
    }

    public void updateEntity(MenuCategory category, UpdateMenuCategoryRequest request) {
        if (request.getCategoryName() != null) category.setCategoryName(request.getCategoryName());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        if (request.getExternalCategoryId() != null) category.setExternalCategoryId(request.getExternalCategoryId());
    }

    public MenuCategoryResponse toResponse(MenuCategory category) {
        return MenuCategoryResponse.builder()
                .id(category.getId())
                .vendorId(category.getVendor().getId())
                .categoryName(category.getCategoryName())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .sourceSystem(category.getSourceSystem())
                .externalCategoryId(category.getExternalCategoryId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}