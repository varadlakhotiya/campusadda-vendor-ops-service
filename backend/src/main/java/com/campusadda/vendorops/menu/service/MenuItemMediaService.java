package com.campusadda.vendorops.menu.service;

import com.campusadda.vendorops.menu.dto.request.CreateMenuItemMediaRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemMediaResponse;

import java.util.List;

public interface MenuItemMediaService {
    MenuItemMediaResponse addMedia(Long vendorId, Long menuItemId, CreateMenuItemMediaRequest request);
    List<MenuItemMediaResponse> getMedia(Long vendorId, Long menuItemId);
    void deleteMedia(Long vendorId, Long menuItemId, Long mediaId);
}