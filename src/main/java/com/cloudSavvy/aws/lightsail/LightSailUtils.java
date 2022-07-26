package com.cloudSavvy.aws.lightsail;

import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;

public class LightSailUtils {
    public static Double getMaxOfMetricMax(List<MetricDatapoint> metrics) {
        if (CollectionUtils.isNullOrEmpty(metrics)) {
            return 0.0;
        }

        return metrics.stream().map(MetricDatapoint::maximum).max(Double::compareTo).orElse(0.0);
    }

    public static Double getMaxOfMetricSum(List<MetricDatapoint> metrics) {
        if (CollectionUtils.isNullOrEmpty(metrics)) {
            return 0.0;
        }

        return metrics.stream().map(MetricDatapoint::sum).max(Double::compareTo).orElse(0.0);
    }
}
