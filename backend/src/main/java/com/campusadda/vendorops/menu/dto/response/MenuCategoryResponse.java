package com.campusadda.vendorops.menu.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MenuCategoryResponse {
    private Long id;
    private Long vendorId;
    private String categoryName;
    private Integer displayOrder;
    private Boolean isActive;
    private String sourceSystem;
    private String externalCategoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}