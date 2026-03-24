package com.campusadda.vendorops.menu.repository;

import com.campusadda.vendorops.menu.entity.MenuItemMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemMediaRepository extends JpaRepository<MenuItemMedia, Long> {
    List<MenuItemMedia> findByMenuItem_IdOrderByDisplayOrderAsc(Long menuItemId);
    Optional<MenuItemMedia> findByIdAndMenuItem_Id(Long id, Long menuItemId);
}