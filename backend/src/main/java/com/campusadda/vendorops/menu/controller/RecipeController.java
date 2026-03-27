package com.campusadda.vendorops.menu.controller;

import com.campusadda.vendorops.common.dto.ApiResponse;
import com.campusadda.vendorops.menu.dto.request.CreateIngredientRequest;
import com.campusadda.vendorops.menu.dto.request.UpdateIngredientRequest;
import com.campusadda.vendorops.menu.dto.response.IngredientResponse;
import com.campusadda.vendorops.menu.dto.response.RecipeValidationResponse;
import com.campusadda.vendorops.menu.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors/{vendorId}/menu-items/{menuItemId}/ingredients")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<ApiResponse<IngredientResponse>> addIngredient(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody CreateIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ingredient added successfully",
                        recipeService.addIngredient(vendorId, menuItemId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IngredientResponse>>> getIngredients(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Ingredients fetched successfully",
                recipeService.getIngredients(vendorId, menuItemId)));
    }

    @PutMapping("/{ingredientId}")
    public ResponseEntity<ApiResponse<IngredientResponse>> updateIngredient(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @PathVariable Long ingredientId,
            @Valid @RequestBody UpdateIngredientRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ingredient updated successfully",
                recipeService.updateIngredient(vendorId, menuItemId, ingredientId, request)));
    }

    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<ApiResponse<Void>> deleteIngredient(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId,
            @PathVariable Long ingredientId) {
        recipeService.deleteIngredient(vendorId, menuItemId, ingredientId);
        return ResponseEntity.ok(ApiResponse.success("Ingredient deleted successfully", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<RecipeValidationResponse>> validateRecipe(
            @PathVariable Long vendorId,
            @PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Recipe validation completed",
                recipeService.validateRecipe(vendorId, menuItemId)));
    }
}