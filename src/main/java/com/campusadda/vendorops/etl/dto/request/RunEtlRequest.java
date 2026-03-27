package com.campusadda.vendorops.etl.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunEtlRequest {
    private String windowStart;
    private String windowEnd;
}