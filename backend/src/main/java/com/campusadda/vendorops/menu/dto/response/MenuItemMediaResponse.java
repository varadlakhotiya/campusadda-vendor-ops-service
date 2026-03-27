package com.campusadda.vendorops.menu.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MenuItemMediaResponse {
    private Long id;
    private Long menuItemId;
    private String mediaType;
    private String mediaUrl;
    private Boolean isPrimary;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}