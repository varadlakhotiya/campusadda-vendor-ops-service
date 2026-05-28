package com.campusadda.vendorops.menu.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuCategory;
import com.campusadda.vendorops.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuCategoryValidator {

    private final MenuCategoryRepository menuCategoryRepository;

    public void validateUniqueCategoryName(Long vendorId, String categoryName) {
        if (menuCategoryRepository.existsByVendor_IdAndCategoryName(vendorId, categoryName)) {
            throw new ConflictException("Category name already exists for this vendor");
        }
    }

    public MenuCategory validateCategoryExists(Long vendorId, Long categoryId) {
        return menuCategoryRepository.findByIdAndVendor_Id(categoryId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));
    }
}