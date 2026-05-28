package com.campusadda.vendorops.publicorder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerCreateOrderRequest {

    @NotNull
    private Long vendorId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerPhone;

    private String notes;

    @Valid
    @NotEmpty
    private List<CustomerOrderItemRequest> items;
}