package com.campusadda.vendorops.menu.validator;

import com.campusadda.vendorops.common.exception.ConflictException;
import com.campusadda.vendorops.common.exception.ResourceNotFoundException;
import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import com.campusadda.vendorops.menu.repository.MenuItemIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeValidator {

    private final MenuItemIngredientRepository ingredientRepository;

    public void validateIngredientNotDuplicate(Long menuItemId, Long inventoryItemId) {
        if (ingredientRepository.existsByMenuItem_IdAndInventoryItem_Id(menuItemId, inventoryItemId)) {
            throw new ConflictException("Ingredient already mapped to menu item");
        }
    }

    public MenuItemIngredient validateIngredientExists(Long menuItemId, Long ingredientId) {
        return ingredientRepository.findByIdAndMenuItem_Id(ingredientId, menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient mapping not found"));
    }
}