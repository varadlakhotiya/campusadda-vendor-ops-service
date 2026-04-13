package com.campusadda.vendorops.menu.mapper;

import com.campusadda.vendorops.menu.dto.request.CreateMenuItemMediaRequest;
import com.campusadda.vendorops.menu.dto.request.CreateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemMediaResponse;
import com.campusadda.vendorops.menu.dto.response.MenuItemResponse;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.entity.MenuItemMedia;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItem toEntity(CreateMenuItemRequest request) {
        MenuItem item = new MenuItem();
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCostPrice(request.getCostPrice());

        // ✅ FIX: Integer → Short
        item.setPrepTimeMinutes(
                request.getPrepTimeMinutes() != null
                        ? request.getPrepTimeMinutes().shortValue()
                        : null
        );

        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : Boolean.TRUE);
        item.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);
        item.setIsVeg(request.getIsVeg() != null ? request.getIsVeg() : Boolean.TRUE);
        item.setTrackInventory(request.getTrackInventory() != null ? request.getTrackInventory() : Boolean.TRUE);
        item.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        item.setPrimaryImageUrl(request.getPrimaryImageUrl());
        item.setSourceSystem(request.getSourceSystem() != null ? request.getSourceSystem() : "VENDOR_OPS");
        item.setExternalMenuItemId(request.getExternalMenuItemId());
        return item;
    }

    public void updateEntity(MenuItem item, UpdateMenuItemRequest request) {
        if (request.getItemName() != null) item.setItemName(request.getItemName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCostPrice() != null) item.setCostPrice(request.getCostPrice());

        // ✅ FIX: Integer → Short
        if (request.getPrepTimeMinutes() != null) {
            item.setPrepTimeMinutes(request.getPrepTimeMinutes().shortValue());
        }

        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
        if (request.getIsActive() != null) item.setIsActive(request.getIsActive());
        if (request.getIsVeg() != null) item.setIsVeg(request.getIsVeg());
        if (request.getTrackInventory() != null) item.setTrackInventory(request.getTrackInventory());
        if (request.getDisplayOrder() != null) item.setDisplayOrder(request.getDisplayOrder());
        if (request.getPrimaryImageUrl() != null) item.setPrimaryImageUrl(request.getPrimaryImageUrl());
        if (request.getExternalMenuItemId() != null) item.setExternalMenuItemId(request.getExternalMenuItemId());
    }

    public MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .vendorId(item.getVendor().getId())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getCategoryName() : null)
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .description(item.getDescription())
                .price(item.getPrice())
                .costPrice(item.getCostPrice())

                // ✅ FIX: Short → Integer
                .prepTimeMinutes(
                        item.getPrepTimeMinutes() != null
                                ? item.getPrepTimeMinutes().intValue()
                                : null
                )

                .isAvailable(item.getIsAvailable())
                .isActive(item.getIsActive())
                .isVeg(item.getIsVeg())
                .trackInventory(item.getTrackInventory())
                .displayOrder(item.getDisplayOrder())
                .primaryImageUrl(item.getPrimaryImageUrl())
                .sourceSystem(item.getSourceSystem())
                .externalMenuItemId(item.getExternalMenuItemId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public MenuItemMedia toMediaEntity(CreateMenuItemMediaRequest request) {
        MenuItemMedia media = new MenuItemMedia();
        media.setMediaType(request.getMediaType() != null ? request.getMediaType() : "IMAGE");
        media.setMediaUrl(request.getMediaUrl());
        media.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : Boolean.FALSE);
        media.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        return media;
    }

    public MenuItemMediaResponse toMediaResponse(MenuItemMedia media) {
        return MenuItemMediaResponse.builder()
                .id(media.getId())
                .menuItemId(media.getMenuItem().getId())
                .mediaType(media.getMediaType())
                .mediaUrl(media.getMediaUrl())
                .isPrimary(media.getIsPrimary())
                .displayOrder(media.getDisplayOrder())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}