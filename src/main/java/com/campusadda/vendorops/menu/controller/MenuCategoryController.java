package com.campusadda.vendorops.menu.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.menu.dto.request.CreateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.response.MenuCategoryResponse;
import com.campusadda.vendorops.menu.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> create(
            @PathVariable Long vendorId,
            @Valid @RequestBody CreateMenuCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu category created successfully",
                        menuCategoryService.createCategory(vendorId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuCategoryResponse>>> list(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Menu categories fetched successfully",
                menuCategoryService.getCategories(vendorId)));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> get(
            @PathVariable Long vendorId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success("Menu category fetched successfully",
                menuCategoryService.getCategoryById(vendorId, categoryId)));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> update(
            @PathVariable Long vendorId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateMenuCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Menu category updated successfully",
                menuCategoryService.updateCategory(vendorId, categoryId, request)));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long vendorId,
            @PathVariable Long categoryId) {
        menuCategoryService.deleteCategory(vendorId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Menu category deleted successfully", null));
    }
}