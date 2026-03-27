package com.campusadda.vendorops.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "Vendor id is required")
    private Long vendorId;

    private String sourceSystem;
    private String externalOrderId;
    private String externalCustomerId;
    private String orderSource;
    private String paymentStatus;
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private String notes;

    @Valid
    @NotEmpty(message = "At least one order item is required")
    private List<CreateOrderItemRequest> items;
}