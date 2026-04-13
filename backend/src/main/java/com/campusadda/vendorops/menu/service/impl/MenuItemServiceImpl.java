package com.campusadda.vendorops.menu.service.impl;

import com.campusadda.vendorops.menu.dto.request.CreateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemAvailabilityRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuItemRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemResponse;
import com.campusadda.vendorops.menu.entity.MenuCategory;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.mapper.MenuItemMapper;
import com.campusadda.vendorops.menu.repository.MenuItemRepository;
import com.campusadda.vendorops.menu.service.MenuItemService;
import com.campusadda.vendorops.menu.validator.MenuCategoryValidator;
import com.campusadda.vendorops.menu.validator.MenuItemValidator;
import com.campusadda.vendorops.security.VendorAccessService;
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemValidator menuItemValidator;
    private final MenuCategoryValidator menuCategoryValidator;
    private final MenuItemMapper menuItemMapper;
    private final VendorValidator vendorValidator;
    private final VendorAccessService vendorAccessService;

    @Override
    public MenuItemResponse createMenuItem(Long vendorId, CreateMenuItemRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        menuItemValidator.validateUniqueItemCode(vendorId, request.getItemCode());

        MenuItem item = menuItemMapper.toEntity(request);
        item.setVendor(vendor);

        if (request.getCategoryId() != null) {
            MenuCategory category = menuCategoryValidator.validateCategoryExists(vendorId, request.getCategoryId());
            item.setCategory(category);
        }

        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(Long vendorId, Long categoryId) {
        vendorAccessService.validateVendorAccess(vendorId);
        vendorValidator.validateVendorExists(vendorId);

        List<MenuItem> items;

        if (categoryId != null) {
            menuCategoryValidator.validateCategoryExists(vendorId, categoryId);
            items = menuItemRepository
                    .findByVendor_IdAndCategory_IdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(vendorId, categoryId);
        } else {
            items = menuItemRepository
                    .findByVendor_IdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(vendorId);
        }

        return items.stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long vendorId, Long menuItemId) {
        vendorAccessService.validateVendorAccess(vendorId);

        return menuItemMapper.toResponse(
                menuItemValidator.validateMenuItemExists(vendorId, menuItemId)
        );
    }

    @Override
    public MenuItemResponse updateMenuItem(Long vendorId, Long menuItemId, UpdateMenuItemRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem item = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        if (request.getCategoryId() != null) {
            MenuCategory category = menuCategoryValidator.validateCategoryExists(vendorId, request.getCategoryId());
            item.setCategory(category);
        }

        menuItemMapper.updateEntity(item, request);
        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }

    @Override
    public MenuItemResponse updateAvailability(Long vendorId, Long menuItemId, UpdateMenuItemAvailabilityRequest request) {
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem item = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        item.setIsAvailable(request.getIsAvailable());
        return menuItemMapper.toResponse(menuItemRepository.save(item));
    }

    @Override
    public void deleteMenuItem(Long vendorId, Long menuItemId) {
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem item = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        item.setIsActive(Boolean.FALSE);
        menuItemRepository.save(item);
    }
}