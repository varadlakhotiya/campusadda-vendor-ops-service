package com.campusadda.vendorops.menu.service;

import com.campusadda.vendorops.menu.dto.request.CreateIngredientRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateIngredientRequest;
import com.campusadda.vendorops.menu.dto.response.IngredientResponse;
import com.campusadda.vendorops.menu.dto.response.RecipeValidationResponse;

import java.util.List;

public interface RecipeService {
    IngredientResponse addIngredient(Long vendorId, Long menuItemId, CreateIngredientRequest request);
    List<IngredientResponse> getIngredients(Long vendorId, Long menuItemId);
    IngredientResponse updateIngredient(Long vendorId, Long menuItemId, Long ingredientId, UpdateIngredientRequest request);
    void deleteIngredient(Long vendorId, Long menuItemId, Long ingredientId);
    RecipeValidationResponse validateRecipe(Long vendorId, Long menuItemId);
}