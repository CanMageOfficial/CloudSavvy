package com.cloudSavvy.aws.metric;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Builder
@Getter
public class MetricTrait {
    @NonNull private String namespace;
    @NonNull private String metricName;
    @NonNull private StandardUnit unit;
    @NonNull private Statistic statistic;
    @NonNull private String dimensionName;
}
