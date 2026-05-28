package com.campusadda.vendorops.menu.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateMenuItemRequest {
    private Long categoryId;
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
    private String externalMenuItemId;
}