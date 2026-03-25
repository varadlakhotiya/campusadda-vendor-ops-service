package com.campusadda.vendorops.menu.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.menu.dto.request.CreateMenuItemMediaRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemMediaResponse;
import com.campusadda.vendorops.menu.service.MenuItemMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/menu-items/{menuItemId}/media")
@RequiredArgsConstructor
public class MenuItemMediaController {

    private final MenuItemMediaService mediaService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemMediaResponse>> addMedia(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody CreateMenuItemMediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item media added successfully",
                        mediaService.addMedia(vendorId, menuItemId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemMediaResponse>>> getMedia(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Menu item media fetched successfully",
                mediaService.getMedia(vendorId, menuItemId)));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @PathVariable Long mediaId) {
        mediaService.deleteMedia(vendorId, menuItemId, mediaId);
        return ResponseEntity.ok(ApiResponse.success("Menu item media deleted successfully", null));
    }
}