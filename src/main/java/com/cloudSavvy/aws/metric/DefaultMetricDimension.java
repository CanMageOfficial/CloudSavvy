package com.cloudSavvy.aws.metric;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DefaultMetricDimension implements MetricDimensionBuilder {
    @Override
    public Collection<Dimension> buildDimensions(final String name, String value,
                                                 final Map<String, String> extraDimensions) {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(Dimension.builder().name(name).value(value).build());
        if (!CollectionUtils.isNullOrEmpty(extraDimensions)) {
            for (Map.Entry<String, String> entry : extraDimensions.entrySet()) {
                dimensions.add(Dimension.builder()
                        .name(entry.getKey()).value(entry.getValue()).build());
            }
        }

        return dimensions;
    }
}
