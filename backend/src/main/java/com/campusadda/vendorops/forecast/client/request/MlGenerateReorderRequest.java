package com.campusadda.vendorops.forecast.client.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MlGenerateReorderRequest {
    private String configPath;
}
