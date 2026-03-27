package com.campusadda.vendorops.inventory.service.impl;

import com.campusadda.vendorops.inventory.dto.response.AvailabilityCheckResponse;
import com.campusadda.vendorops.menu.entity.MenuItem;
import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import com.campusadda.vendorops.menu.repository.MenuItemIngredientRepository;
import com.campusadda.vendorops.menu.validator.MenuItemValidator;
import com.campusadda.vendorops.inventory.service.StockCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockCheckServiceImpl implements StockCheckService {

    private final MenuItemValidator menuItemValidator;
    private final MenuItemIngredientRepository ingredientRepository;

    @Override
    public AvailabilityCheckResponse checkMenuItemSellability(Long vendorId, Long menuItemId) {
        MenuItem menuItem = menuItemValidator.validateMenuItemExists(vendorId, menuItemId);
        List<MenuItemIngredient> ingredients = ingredientRepository.findByMenuItem_Id(menuItemId);

        List<String> issues = new ArrayList<>();

        if (!menuItem.getIsAvailable()) {
            issues.add("Menu item is marked unavailable");
        }

        if (!menuItem.getIsActive()) {
            issues.add("Menu item is inactive");
        }

        if (Boolean.TRUE.equals(menuItem.getTrackInventory())) {
            if (ingredients.isEmpty()) {
                issues.add("No ingredients mapped for inventory tracking");
            } else {
                ingredients.stream()
                        .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                        .forEach(i -> {
                            BigDecimal required = i.getQuantityRequired();
                            BigDecimal current = i.getInventoryItem().getCurrentQuantity();
                            if (current.compareTo(required) < 0 && !Boolean.TRUE.equals(i.getIsOptional())) {
                                issues.add("Insufficient stock for " + i.getInventoryItem().getItemName());
                            }
                        });
            }
        }

        return AvailabilityCheckResponse.builder()
                .menuItemId(menuItem.getId())
                .menuItemName(menuItem.getItemName())
                .sellable(issues.isEmpty())
                .issues(issues)
                .build();
    }
}