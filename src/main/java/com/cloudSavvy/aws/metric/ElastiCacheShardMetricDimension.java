package com.cloudSavvy.aws.metric;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ElastiCacheShardMetricDimension implements MetricDimensionBuilder {
    @Override
    public Collection<Dimension> buildDimensions(final String name, String value,
                                                 final Map<String, String> extraDimensions) {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        String[] tokens = value.split("\\|");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("dimension value is not in correct format:" + value);
        }
        dimensions.add(Dimension.builder().name(name).value(tokens[0]).build());

        dimensions.add(Dimension.builder()
                .name("NodeGroupId").value(tokens[1]).build());
        return dimensions;
    }

    public static String buildDimensionGroup(final String repGroupId, final String nodeGroupId) {
        return repGroupId + "|" + nodeGroupId;
    }
}
