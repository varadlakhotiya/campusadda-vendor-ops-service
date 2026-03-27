package com.campusadda.vendorops.menu.mapper;

import com.campusadda.vendorops.menu.dto.response.IngredientResponse;
import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    public IngredientResponse toResponse(MenuItemIngredient ingredient) {
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .menuItemId(ingredient.getMenuItem().getId())
                .inventoryItemId(ingredient.getInventoryItem().getId())
                .inventoryItemCode(ingredient.getInventoryItem().getItemCode())
                .inventoryItemName(ingredient.getInventoryItem().getItemName())
                .inventoryUnit(ingredient.getInventoryItem().getUnit())
                .quantityRequired(ingredient.getQuantityRequired())
                .wastagePct(ingredient.getWastagePct())
                .isOptional(ingredient.getIsOptional())
                .isActive(ingredient.getIsActive())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .build();
    }
}