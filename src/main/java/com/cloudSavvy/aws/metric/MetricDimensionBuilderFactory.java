package com.cloudSavvy.aws.metric;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MetricDimensionBuilderFactory {
    private DefaultMetricDimension defaultMetricDimension;
    private ElastiCacheShardMetricDimension elastiCacheShardMetricDimension;
    private CloudSearchClientDimension cloudSearchClientDimension;
    private BillingEstimatedChargesDimension estimatedChargesDimension;

    public MetricDimensionBuilder getMetricDimensionBuilder(AWSMetric awsMetric) {
        if (awsMetric == AWSMetric.ELASTICACHE_SHARD_CPU_UTILIZATION
                || awsMetric == AWSMetric.ELASTICACHE_SHARD_MEMORY) {
            return elastiCacheShardMetricDimension;
        } else if (awsMetric == AWSMetric.CLOUDSEARCH_SUCCESSFUL_REQUESTS) {
            return cloudSearchClientDimension;
        } else if (awsMetric == AWSMetric.BILLING_ESTIMATED_CHARGES) {
            return estimatedChargesDimension;
        }
        return defaultMetricDimension;
    }
}
