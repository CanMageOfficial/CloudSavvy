package com.cloudSavvy.aws.metric;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CloudSearchClientDimension implements MetricDimensionBuilder {
    @Override
    public Collection<Dimension> buildDimensions(final String name, String value,
                                                 final Map<String, String> extraDimensions) {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(Dimension.builder().name(name).value(value).build());
        if (!extraDimensions.containsKey(value)) {
            throw new IllegalArgumentException("ClientID is missing for CloudSearch:" + value);
        }

        String[] tokens = extraDimensions.get(value).split("\\|");
        for (String token : tokens) {
            dimensions.add(Dimension.builder().name("ClientId").value(token).build());
        }
        return dimensions;
    }
}
