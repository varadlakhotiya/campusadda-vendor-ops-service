package com.campusadda.vendorops.etl.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunEtlRequest {

    @NotBlank
    private String windowStart;

    @NotBlank
    private String windowEnd;
}
