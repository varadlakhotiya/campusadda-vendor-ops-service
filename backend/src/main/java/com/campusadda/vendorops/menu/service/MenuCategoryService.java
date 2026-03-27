package com.campusadda.vendorops.menu.service;

import com.campusadda.vendorops.menu.dto.request.CreateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.response.MenuCategoryResponse;

import java.util.List;

public interface MenuCategoryService {
    MenuCategoryResponse createCategory(Long vendorId, CreateMenuCategoryRequest request);
    List<MenuCategoryResponse> getCategories(Long vendorId);
    MenuCategoryResponse getCategoryById(Long vendorId, Long categoryId);
    MenuCategoryResponse updateCategory(Long vendorId, Long categoryId, UpdateMenuCategoryRequest request);
    void deleteCategory(Long vendorId, Long categoryId);
}