package com.cloudSavvy.utils;

import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Collections;

public class MetricUtils {
    private static final long BYTE = 1L;
    private static final long KiB = BYTE << 10;
    private static final long MiB = KiB << 10;
    private static final long GiB = MiB << 10;

    public static Double getMax(MetricDataResult dataResult) {
        if (dataResult == null || CollectionUtils.isNullOrEmpty(dataResult.values())) {
            return 0.0;
        }
        return Collections.max(dataResult.values());
    }

    public static int getMetricSize(MetricDataResult dataResult) {
        if (dataResult == null || CollectionUtils.isNullOrEmpty(dataResult.values())) {
            return 0;
        }
        return dataResult.values().size();
    }

    public static Double convertToGiB(Double byteData) {
        return byteData / (GiB);
    }
}
