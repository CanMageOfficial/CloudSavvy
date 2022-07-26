package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.metric.AWSMetric;
import com.cloudSavvy.aws.metric.MetricConstants;
import com.cloudSavvy.aws.metric.MetricDimensionBuilder;
import com.cloudSavvy.aws.metric.MetricDimensionBuilderFactory;
import com.cloudSavvy.aws.metric.MetricTrait;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.cloudwatch.model.StatusCode;
import software.amazon.awssdk.services.cloudwatch.paginators.ListMetricsIterable;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class CloudWatchAccessor {

    private CloudWatchClient cloudWatchClient;
    private MetricDimensionBuilderFactory dimensionBuilderFactory;

    public List<Metric> listEstimatedChargesMetrics() {
        List<Metric> metrics = new ArrayList<>();
        ListMetricsRequest request = ListMetricsRequest.builder().namespace("AWS/Billing")
                .metricName("EstimatedCharges").build();
        ListMetricsIterable metricsResponse = cloudWatchClient.listMetricsPaginator(request);
        for (Metric metric : metricsResponse.metrics()) {
            metrics.add(metric);

            if (metrics.size() > 1000) {
                break;
            }
        }
        return metrics;
    }

    public List<Metric> listMetrics(MetricTrait metricTrait) {
        ListMetricsRequest metricsRequest = ListMetricsRequest.builder()
                .namespace(metricTrait.getNamespace()).metricName(metricTrait.getMetricName()).build();
        ListMetricsResponse metricsResponse = cloudWatchClient.listMetrics(metricsRequest);
        return metricsResponse.metrics();
    }

    public Map<String, MetricDataResult> getEstimatedChargesMetricData(final List<String> serviceNames,
                                                                       final Map<String, String> extraDimensions) {
        return getMetricData(AWSMetric.BILLING_ESTIMATED_CHARGES, serviceNames, extraDimensions);
    }

    public Map<String, MetricDataResult> getKinesisZeppelinServerUptimeMetricData(final List<String> applications) {
        return getMetricData(AWSMetric.KINESIS_ZEPPELIN_SERVER_UPTIME, applications, null);
    }

    public Map<String, MetricDataResult> getCloudSearchSuccessfulRequestsMetricData(List<String> dimensionNames,
                                                                                    Map<String, String> extraDimensions) {
        return getMetricData(AWSMetric.CLOUDSEARCH_SUCCESSFUL_REQUESTS, dimensionNames, extraDimensions);
    }

    public Map<String, MetricDataResult> getAppStreamCapUtilizationMetricData(List<String> fleets) {
        return getMetricData(AWSMetric.APP_STREAM_CAPACITY_UTILIZATION, fleets, null);
    }

    public Map<String, MetricDataResult> getFSXFreeStorageCapMetricData(List<String> fileSystemIds) {
        return getMetricData(AWSMetric.FSX_FREE_STORAGE_CAPACITY, fileSystemIds, null);
    }

    public Map<String, MetricDataResult> getFSXFreeDataStorageCapMetricData(List<String> fileSystemIds) {
        return getMetricData(AWSMetric.FSX_FREE_DATA_STORAGE_CAPACITY, fileSystemIds, null);
    }

    public Map<String, MetricDataResult> getFSXStorageUsedMetricData(List<String> fileSystemIds) {
        return getMetricData(AWSMetric.FSX_STORAGE_USED, fileSystemIds, null);
    }

    public Map<String, MetricDataResult> getFSXUsedStorageCapacityMetricData(List<String> fileSystemIds) {
        return getMetricData(AWSMetric.FSX_USED_STORAGE_CAPACITY, fileSystemIds, null);
    }

    public Map<String, MetricDataResult> getEFSClientConnectionsMetricData(List<String> fileSystemIds) {
        return getMetricData(AWSMetric.EFS_CLIENT_CONNECTIONS, fileSystemIds, null);
    }

    public Map<String, MetricDataResult> getMemoryDbClusterNewConnectionsMetricData(List<String> cacheClusterIds) {
        return getMetricData(AWSMetric.MEMORY_DB_CLUSTER_NEW_CONNECTIONS, cacheClusterIds, null);
    }

    public Map<String, MetricDataResult> getElastiCacheClusterNewConnectionsMetricData(List<String> cacheClusterIds) {
        return getMetricData(AWSMetric.ELASTICACHE_CLUSTER_NEW_CONNECTIONS, cacheClusterIds, null);
    }

    public Map<String, MetricDataResult> getElastiCacheShardCPUUtilMetricData(List<String> replicationGroupIds) {
        return getMetricData(AWSMetric.ELASTICACHE_SHARD_CPU_UTILIZATION, replicationGroupIds, null);
    }

    public Map<String, MetricDataResult> getElastiCacheShardMemoryMetricData(List<String> replicationGroupIds) {
        return getMetricData(AWSMetric.ELASTICACHE_SHARD_MEMORY, replicationGroupIds, null);
    }

    public Map<String, MetricDataResult> getS3BucketSizeBytesMetricData(List<String> bucketNames) {
        Map<String, String> extraDimensions = Collections.singletonMap("StorageType", "StandardStorage");
        return getMetricData(AWSMetric.S3_BUCKET_SIZE_BYTES, bucketNames, extraDimensions);
    }

    public Map<String, MetricDataResult> getCloudfrontDistRequestsMetricData(List<String> distributionIds) {
        Map<String, String> extraDimensions = Collections.singletonMap("Region", "Global");
        return getMetricData(AWSMetric.CLOUDFRONT_DISTRIBUTION_REQUESTS, distributionIds, extraDimensions);
    }

    public Map<String, MetricDataResult> getRedshiftClusterCPUUtilMetricData(List<String> clusterIds) {
        return getMetricData(AWSMetric.REDSHIFT_CLUSTER_CPU_UTILIZATION, clusterIds, null);
    }

    public Map<String, MetricDataResult> getRedshiftClusterDbConnectionsMetricData(List<String> clusterIds) {
        return getMetricData(AWSMetric.REDSHIFT_CLUSTER_DATABASE_CONNECTIONS, clusterIds, null);
    }

    public Map<String, MetricDataResult> getRedshiftServerlessComputeSecsMetricData(List<String> workgroupNames) {
        return getMetricData(AWSMetric.REDSHIFT_SERVERLESS_COMPUTE_SECONDS, workgroupNames, null);
    }

    public Map<String, MetricDataResult> getRDSClusterDBConnectionsMetricData(List<String> clusterIds) {
        return getMetricData(AWSMetric.RDS_CLUSTER_DB_CONNECTIONS, clusterIds, null);
    }

    public Map<String, MetricDataResult> getRDSInstanceDBConnectionsMetricData(List<String> instanceIds) {
        return getMetricData(AWSMetric.RDS_INSTANCE_DB_CONNECTIONS, instanceIds, null);
    }

    public Map<String, MetricDataResult> getRDSProxyDBConnectionsMetricData(List<String> proxyNames) {
        return getMetricData(AWSMetric.RDS_PROXY_DB_CONNECTIONS, proxyNames, null);
    }

    public Map<String, MetricDataResult> getKinesisGetRecordRecordsMetricData(List<String> streamNames) {
        return getMetricData(AWSMetric.KINESIS_GET_RECORDS_RECORDS, streamNames, null);
    }

    public Map<String, MetricDataResult> getAppLBRequestCountMetricData(List<String> lbMetricNames) {
        return getMetricData(AWSMetric.APPLICATION_ELB_REQUEST_COUNT, lbMetricNames, null);
    }

    public Map<String, MetricDataResult> getNetworkLBNewFlowCountMetricData(List<String> lbMetricNames) {
        return getMetricData(AWSMetric.NETWORK_ELB_NEW_FLOW_COUNT, lbMetricNames, null);
    }

    public Map<String, MetricDataResult> getEC2InstanceCPUUtilMetricData(List<String> instanceIds) {
        return getMetricData(AWSMetric.EC2_INSTANCE_CPU_UTILIZATION, instanceIds, null);
    }

    public Map<String, MetricDataResult> getLambdaInvocationsMetricData(List<String> functionNames) {
        return getMetricData(AWSMetric.LAMBDA_INVOCATIONS, functionNames, null);
    }

    public Map<String, MetricDataResult> getNatGatewayConnectionsMetricData(List<String> gatewayIds) {
        return getMetricData(AWSMetric.NAT_GATEWAY_ACTIVE_CONNECTIONS, gatewayIds, null);
    }

    public Map<String, MetricDataResult> getMetricData(
            AWSMetric awsMetric, List<String> resourceIds,
            Map<String, String> extraDimensions) {
        if (CollectionUtils.isNullOrEmpty(resourceIds)) {
            return new HashMap<>();
        }

        if (!MetricConstants.AWSMetricTraits.containsKey(awsMetric)
                || MetricConstants.AWSMetricTraits.get(awsMetric) == null) {
            throw new IllegalArgumentException("Unknown AWSMetric:" + awsMetric);
        }
        MetricTrait metricTrait = MetricConstants.AWSMetricTraits.get(awsMetric);

        Collection<MetricDataQuery> metricDataQueries = new ArrayList<>();
        Map<String, MetricDataResult> metricNameToDataResultMap = new HashMap<>();
        Map<String, String> metricIdToNameMap = new HashMap<>();
        for (String resourceId : resourceIds) {
            String metricId = generateMetricId(resourceId);
            metricIdToNameMap.put(metricId, resourceId);
            MetricDimensionBuilder dimensionBuilder = dimensionBuilderFactory.getMetricDimensionBuilder(awsMetric);
            Collection<Dimension> dimensions =
                    dimensionBuilder.buildDimensions(Objects.requireNonNull(metricTrait).getDimensionName(), resourceId, extraDimensions);
            metricDataQueries.add(buildMetricDataQuery(metricTrait, dimensions, metricId));
        }

        GetMetricDataRequest metricStatisticsRequest = GetMetricDataRequest.builder()
                .metricDataQueries(metricDataQueries)
                .startTime(Instant.now().minusSeconds(MetricConstants.SECONDS_IN_TWO_WEEKS))
                .endTime(Instant.now())
                .build();

        GetMetricDataResponse response = cloudWatchClient.getMetricData(metricStatisticsRequest);
        logError(response.metricDataResults(), metricTrait);
        for (MetricDataResult metricData : response.metricDataResults()) {
            metricNameToDataResultMap.put(metricIdToNameMap.get(metricData.id()), metricData);
        }
        return metricNameToDataResultMap;
    }

    private void logError(List<MetricDataResult> results, MetricTrait metricTrait) {
        Optional<MetricDataResult> resultOptional =
                results.stream().filter(result -> result.statusCode() != StatusCode.COMPLETE).findFirst();
        resultOptional.ifPresent(dataResult -> log.error("Metric retrieval failed for {} namespace, metric: {}, error: {}",
                metricTrait.getNamespace(), metricTrait.getMetricName(), dataResult.statusCode()));
    }

    public String generateMetricId(String functionName) {
        String code = String.valueOf(functionName.hashCode()).replace("-", "_");
        return "id" + code;
    }

    private MetricDataQuery buildMetricDataQuery(MetricTrait metricTrait,
                                                 Collection<Dimension> dimensions,
                                                 String metricId) {
        MetricStat metricStat = MetricStat.builder()
                .metric(Metric.builder().metricName(metricTrait.getMetricName())
                        .namespace(metricTrait.getNamespace())
                        .dimensions(dimensions)
                        .build())
                .period(MetricConstants.SECONDS_IN_DAY)
                .unit(metricTrait.getUnit())
                .stat(metricTrait.getStatistic().toString()).build();
        return MetricDataQuery.builder()
                .metricStat(metricStat)
                .id(metricId).build();
    }
}
