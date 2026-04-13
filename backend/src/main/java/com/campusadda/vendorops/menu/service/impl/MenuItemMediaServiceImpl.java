package com.campusadda.vendorops.menu.service.impl;

import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.dto.request.CreateMenuItemMediaRequest;
import com.campusadda.vendorops.menu.dto.response.MenuItemMediaResponse;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.entity.MenuItemMedia;
import com.campusadda.vendorops.menu.mapper.MenuItemMapper;
import com.campusadda.vendorops.menu.repository.MenuItemMediaRepository;
import com.campusadda.vendorops.menu.service.MenuItemMediaService;
import com.campusadda.vendorops.menu.validator.MenuItemValidator;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADDED
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemMediaServiceImpl implements MenuItemMediaService {

    private final MenuItemMediaRepository mediaRepository;
    private final MenuItemValidator menuItemValidator;
    private final MenuItemMapper menuItemMapper;
    private final VendorAccessService vendorAccessService; // ✅ ADDED

    @Override
    public MenuItemMediaResponse addMedia(Long vendorId, Long menuItemId, CreateMenuItemMediaRequest request) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem menuItem = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        MenuItemMedia media = menuItemMapper.toMediaEntity(request);
        media.setMenuItem(menuItem);

        return menuItemMapper.toMediaResponse(mediaRepository.save(media));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemMediaResponse> getMedia(Long vendorId, Long menuItemId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        return mediaRepository.findByMenuItem_IdOrderByDisplayOrderAsc(menuItemId)
                .stream()
                .map(menuItemMapper::toMediaResponse)
                .toList();
    }

    @Override
    public void deleteMedia(Long vendorId, Long menuItemId, Long mediaId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        MenuItemMedia media = mediaRepository.findByIdAndMenuItem_Id(mediaId, menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item media not found"));

        mediaRepository.delete(media);
    }
}