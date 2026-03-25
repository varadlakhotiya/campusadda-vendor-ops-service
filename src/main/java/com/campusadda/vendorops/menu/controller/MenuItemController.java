package com.campusadda.vendorops.menu.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.menu.dto.request.CreateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemAvailabilityRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemResponse;
import com.campusadda.vendorops.menu.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> create(
            @PathVariable Long vendorId,
            @Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item created successfully",
                        menuItemService.createMenuItem(vendorId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> list(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Menu items fetched successfully",
                menuItemService.getMenuItems(vendorId)));
    }

    @GetMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> get(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Menu item fetched successfully",
                menuItemService.getMenuItemById(vendorId, menuItemId)));
    }

    @PutMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> update(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully",
                menuItemService.updateMenuItem(vendorId, menuItemId, request)));
    }

    @PatchMapping("/{menuItemId}/availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateAvailability(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateMenuItemAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Menu item availability updated successfully",
                menuItemService.updateAvailability(vendorId, menuItemId, request)));
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(vendorId, menuItemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully", null));
    }
}