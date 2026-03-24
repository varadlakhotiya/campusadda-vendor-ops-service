package com.campusadda.vendorops.menu.repository;

import com.campusadda.vendorops.menu.entity.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
    List<MenuItemIngredient> findByMenuItem_Id(Long menuItemId);
    boolean existsByMenuItem_IdAndInventoryItem_Id(Long menuItemId, Long inventoryItemId);
    Optional<MenuItemIngredient> findByIdAndMenuItem_Id(Long ingredientId, Long menuItemId);
}