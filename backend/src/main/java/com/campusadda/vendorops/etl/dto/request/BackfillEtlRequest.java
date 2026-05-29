package com.campusadda.vendorops.etl.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BackfillEtlRequest {

    @NotBlank
    private String fromDate;

    @NotBlank
    private String toDate;
}