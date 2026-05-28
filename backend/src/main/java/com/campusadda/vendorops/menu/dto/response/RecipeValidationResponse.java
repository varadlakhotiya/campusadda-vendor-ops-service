package com.campusadda.vendorops.menu.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecipeValidationResponse {
    private Long menuItemId;
    private String itemName;
    private Boolean recipeReady;
    private List<String> issues;
}