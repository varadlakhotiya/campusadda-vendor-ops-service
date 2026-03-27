package com.campusadda.vendorops.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateMenuItemRequest {
    private Long categoryId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    private String itemName;

    private String description;

    @NotNull(message = "Price is required")
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
}