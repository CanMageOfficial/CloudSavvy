package com.cloudSavvy.aws.billing;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class EstimatedChargesMetric {
    private List<String> serviceNames;
    private String currency;

    public List<String> getServiceNames() {
        return new ArrayList<>(serviceNames);
    }
}
