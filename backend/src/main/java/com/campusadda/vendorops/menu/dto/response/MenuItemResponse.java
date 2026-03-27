package com.campusadda.vendorops.menu.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MenuItemResponse {
    private Long id;
    private Long vendorId;
    private Long categoryId;
    private String categoryName;
    private String itemCode;
    private String itemName;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer prepTimeMinutes;
    private Boolean isAvailable;
    private Boolean isActive;
    private Boolean isVeg;
    private Boolean trackInventory;
    private Integer displayOrder;
    private String primaryImageUrl;
    private String sourceSystem;
    private String externalMenuItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}