package com.campusadda.vendorops.publicorder.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PublicMenuItemResponse {
    private Long id;
    private Long categoryId;
    private String itemCode;
    private String itemName;
    private String description;
    private BigDecimal price;
    private Integer prepTimeMinutes;
    private Boolean isVeg;
    private String primaryImageUrl;
}