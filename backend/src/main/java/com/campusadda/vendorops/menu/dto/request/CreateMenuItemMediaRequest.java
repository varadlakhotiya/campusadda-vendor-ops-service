package com.campusadda.vendorops.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMenuItemMediaRequest {
    private String mediaType;
    @NotBlank(message = "Media URL is required")
    private String mediaUrl;
    private Boolean isPrimary;
    private Integer displayOrder;
}