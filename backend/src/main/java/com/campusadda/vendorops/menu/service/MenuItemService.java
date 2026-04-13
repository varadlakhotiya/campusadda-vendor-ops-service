package com.campusadda.vendorops.menu.service;

import com.campusadda.vendorops.menu.dto.request.CreateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemAvailabilityRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuItemService {
    MenuItemResponse createMenuItem(Long vendorId, CreateMenuItemRequest request);
    List<MenuItemResponse> getMenuItems(Long vendorId, Long categoryId);
    MenuItemResponse getMenuItemById(Long vendorId, Long menuItemId);
    MenuItemResponse updateMenuItem(Long vendorId, Long menuItemId, UpdateMenuItemRequest request);
    MenuItemResponse updateAvailability(Long vendorId, Long menuItemId, UpdateMenuItemAvailabilityRequest request);
    void deleteMenuItem(Long vendorId, Long menuItemId);
}