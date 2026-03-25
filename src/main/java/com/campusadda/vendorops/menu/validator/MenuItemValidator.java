package com.campusadda.vendorops.menu.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuItemValidator {

    private final MenuItemRepository menuItemRepository;

    public void validateUniqueItemCode(Long vendorId, String itemCode) {
        if (menuItemRepository.existsByVendor_IdAndItemCode(vendorId, itemCode)) {
            throw new ConflictException("Menu item code already exists for this vendor");
        }
    }

    public MenuItem validateMenuItemExists(Long vendorId, Long menuItemId) {
        return menuItemRepository.findByIdAndVendor_Id(menuItemId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }
}