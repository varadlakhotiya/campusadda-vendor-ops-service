package com.campusadda.vendorops.menu.service.impl;

import com.campusadda.vendorops.menu.dto.request.CreateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateMenuCategoryRequest;
import com.campusadda.vendorops.menu.dto.response.MenuCategoryResponse;
import com.campusadda.vendorops.menu.entity.MenuCategory;
import com.campusadda.vendorops.menu.mapper.MenuCategoryMapper;
import com.campusadda.vendorops.menu.repository.MenuCategoryRepository;
import com.campusadda.vendorops.menu.service.MenuCategoryService;
import com.campusadda.vendorops.menu.validator.MenuCategoryValidator;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADDED
import com.campusadda.vendorops.vendor.entity.Vendor;
import com.campusadda.vendorops.vendor.validator.VendorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuCategoryServiceImpl implements MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuCategoryValidator menuCategoryValidator;
    private final MenuCategoryMapper menuCategoryMapper;
    private final VendorValidator vendorValidator;
    private final VendorAccessService vendorAccessService; // ✅ ADDED

    @Override
    public MenuCategoryResponse createCategory(Long vendorId, CreateMenuCategoryRequest request) {

        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        Vendor vendor = vendorValidator.validateVendorExists(vendorId);
        menuCategoryValidator.validateUniqueCategoryName(vendorId, request.getCategoryName());

        MenuCategory category = menuCategoryMapper.toEntity(request);
        category.setVendor(vendor);

        return menuCategoryMapper.toResponse(menuCategoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getCategories(Long vendorId) {

        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        vendorValidator.validateVendorExists(vendorId);
        return menuCategoryRepository.findByVendor_IdOrderByDisplayOrderAsc(vendorId)
                .stream()
                .map(menuCategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuCategoryResponse getCategoryById(Long vendorId, Long categoryId) {

        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        return menuCategoryMapper.toResponse(
                menuCategoryValidator.validateCategoryExists(vendorId, categoryId)
        );
    }

    @Override
    public MenuCategoryResponse updateCategory(Long vendorId, Long categoryId, UpdateMenuCategoryRequest request) {

        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        MenuCategory category = menuCategoryValidator.validateCategoryExists(vendorId, categoryId);
        menuCategoryMapper.updateEntity(category, request);
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long vendorId, Long categoryId) {

        vendorAccessService.validateVendorAccess(vendorId); // 🔐 ADD

        MenuCategory category = menuCategoryValidator.validateCategoryExists(vendorId, categoryId);
        category.setIsActive(Boolean.FALSE);
        menuCategoryRepository.save(category);
    }
}