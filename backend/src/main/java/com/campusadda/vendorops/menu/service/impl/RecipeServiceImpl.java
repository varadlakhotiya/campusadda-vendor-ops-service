package com.campusadda.vendorops.menu.service.impl;

import com.campusadda.vendorops.inventory.entity.InventoryItem;
import com.campusadda.vendorops.inventory.validator.InventoryValidator;
import com.campusadda.vendorops.menu.dto.request.CreateIngredientRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateIngredientRequest;
import com.campusadda.vendorops.menu.dto.response.IngredientResponse;
import com.campusadda.vendorops.menu.dto.response.RecipeValidationResponse;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import com.campusadda.vendorops.menu.mapper.RecipeMapper;
import com.campusadda.vendorops.menu.repository.MenuItemIngredientRepository;
import com.campusadda.vendorops.menu.service.RecipeService;
import com.campusadda.vendorops.menu.validator.MenuItemValidator;
import com.campusadda.vendorops.menu.validator.RecipeValidator;
import com.campusadda.vendorops.security.VendorAccessService; // ✅ ADDED
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final MenuItemValidator menuItemValidator;
    private final InventoryValidator inventoryValidator;
    private final RecipeValidator recipeValidator;
    private final MenuItemIngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;
    private final VendorAccessService vendorAccessService; // ✅ ADDED

    @Override
    public IngredientResponse addIngredient(Long vendorId, Long menuItemId, CreateIngredientRequest request) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem menuItem = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        InventoryItem inventoryItem = inventoryValidator.validateInventoryItemExists(vendorId, request.getInventoryItemId());

        recipeValidator.validateIngredientNotDuplicate(menuItemId, inventoryItem.getId());

        MenuItemIngredient ingredient = new MenuItemIngredient();
        ingredient.setMenuItem(menuItem);
        ingredient.setInventoryItem(inventoryItem);
        ingredient.setQuantityRequired(request.getQuantityRequired());
        ingredient.setWastagePct(request.getWastagePct() != null ? request.getWastagePct() : BigDecimal.ZERO);
        ingredient.setIsOptional(request.getIsOptional() != null ? request.getIsOptional() : Boolean.FALSE);
        ingredient.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

        return recipeMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getIngredients(Long vendorId, Long menuItemId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        menuItemValidator.validateMenuItemExists(vendorId, menuItemId);

        return ingredientRepository.findByMenuItem_Id(menuItemId)
                .stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

    @Override
    public IngredientResponse updateIngredient(Long vendorId, Long menuItemId, Long ingredientId, UpdateIngredientRequest request) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        MenuItemIngredient ingredient = recipeValidator.validateIngredientExists(menuItemId, ingredientId);

        if (request.getQuantityRequired() != null) ingredient.setQuantityRequired(request.getQuantityRequired());
        if (request.getWastagePct() != null) ingredient.setWastagePct(request.getWastagePct());
        if (request.getIsOptional() != null) ingredient.setIsOptional(request.getIsOptional());
        if (request.getIsActive() != null) ingredient.setIsActive(request.getIsActive());

        return recipeMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Override
    public void deleteIngredient(Long vendorId, Long menuItemId, Long ingredientId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        MenuItemIngredient ingredient = recipeValidator.validateIngredientExists(menuItemId, ingredientId);
        ingredientRepository.delete(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeValidationResponse validateRecipe(Long vendorId, Long menuItemId) {

        // 🔐 Vendor access validation
        vendorAccessService.validateVendorAccess(vendorId);

        MenuItem menuItem = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        List<MenuItemIngredient> ingredients = ingredientRepository.findByMenuItem_Id(menuItemId);

        List<String> issues = new ArrayList<>();

        if (ingredients.isEmpty()) {
            issues.add("No ingredients mapped");
        }

        ingredients.forEach(i -> {
            if (i.getQuantityRequired() == null || i.getQuantityRequired().compareTo(BigDecimal.ZERO) <= 0) {
                issues.add("Invalid quantity for ingredient: " + i.getInventoryItem().getItemName());
            }
        });

        return RecipeValidationResponse.builder()
                .menuItemId(menuItem.getId())
                .itemName(menuItem.getItemName())
                .recipeReady(issues.isEmpty())
                .issues(issues)
                .build();
    }
}