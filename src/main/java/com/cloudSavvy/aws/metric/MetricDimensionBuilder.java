package com.cloudSavvy.aws.metric;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.Collection;
import java.util.Map;

public interface MetricDimensionBuilder {
    Collection<Dimension> buildDimensions(String name, String value,
                                          Map<String, String> extraDimensions);
}
