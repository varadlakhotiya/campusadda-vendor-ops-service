package com.campusadda.vendorops.forecast.client.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictAndPersistRequest {
    private String configPath;
    private Integer horizon;
}