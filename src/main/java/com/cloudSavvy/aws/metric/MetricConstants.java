package com.cloudSavvy.aws.metric;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

public class MetricConstants {
    public static final int SECONDS_IN_HOUR = 60 * 60;
    public static final int SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;
    public static final int MILLISECONDS_IN_20_HOURS = 20 * MetricConstants.SECONDS_IN_HOUR * 1000;
    public static final int SECONDS_IN_TWO_WEEKS = 14 * SECONDS_IN_DAY;
    public static final String LAMBDA_NAMESPACE = "AWS/Lambda";
    public static final String LAMBDA_INVOCATIONS = "Invocations";

    public static final String NAT_GATEWAY_NAMESPACE = "AWS/NATGateway";
    public static final String GATEWAY_ACTIVE_CONNECTIONS = "ActiveConnectionCount";

    public static final String EC2_NAMESPACE = "AWS/EC2";
    public static final String CPU_UTILIZATION = "CPUUtilization";

    public static final String APPLICATION_ELB_NAMESPACE = "AWS/ApplicationELB";
    public static final String REQUEST_COUNT = "RequestCount";

    public static final String NETWORK_ELB_NAMESPACE = "AWS/NetworkELB";
    public static final String NEW_FLOW_COUNT = "NewFlowCount";

    public static final String KINESIS_NAMESPACE = "AWS/Kinesis";
    public static final String GET_RECORDS_RECORDS = "GetRecords.Records";

    public static final String RDS_NAMESPACE = "AWS/RDS";
    public static final String DB_CONNECTIONS = "DatabaseConnections";

    public static final String REDSHIFT_SERVERLESS_NAMESPACE = "AWS/Redshift-Serverless";
    public static final String COMPUTE_SECONDS = "ComputeSeconds";

    public static final String REDSHIFT_NAMESPACE = "AWS/Redshift";
    public static final String DATABASE_CONNECTIONS = "DatabaseConnections";

    public static final String CLOUDFRONT_NAMESPACE = "AWS/CloudFront";
    public static final String REQUESTS = "Requests";

    public static final String S3_NAMESPACE = "AWS/S3";
    public static final String BUCKET_SIZE_BYTES = "BucketSizeBytes";

    public static final String ELASTICACHE_NAMESPACE = "AWS/ElastiCache";
    public static final String ENGINE_CPU_UTILIZATION = "EngineCPUUtilization";
    public static final String ELASTICACHE_MEMORY_METRIC = "DatabaseMemoryUsageCountedForEvictPercentage";
    public static final String NEW_CONNECTIONS = "NewConnections";

    public static final String EFS_NAMESPACE = "AWS/EFS";
    public static final String CLIENT_CONNECTIONS = "ClientConnections";

    public static final String FSX_NAMESPACE = "AWS/FSx";
    public static final String USED_STORAGE_CAPACITY = "UsedStorageCapacity";
    public static final String STORAGE_USED = "StorageUsed";
    public static final String FREE_DATA_STORAGE_CAPACITY = "FreeDataStorageCapacity";
    public static final String FREE_STORAGE_CAPACITY = "FreeStorageCapacity";
    public static final String APP_STREAM_NAMESPACE = "AWS/AppStream";
    public static final String CAPACITY_UTILIZATION = "CapacityUtilization";

    public static final String CLOUDSEARCH_NAMESPACE = "AWS/CloudSearch";
    public static final String SUCCESSFUL_REQUESTS = "SuccessfulRequests";

    public static final String KINESIS_ANALYTICS_NAMESPACE = "AWS/KinesisAnalytics";
    public static final String ZEPPELIN_SERVER_UPTIME = "zeppelinServerUptime";

    public static final String BILLING_NAMESPACE = "AWS/Billing";
    public static final String ESTIMATED_CHARGES = "EstimatedCharges";
    public static final String MEMORY_DB_NAMESPACE = "AWS/MemoryDB";

    public static final ImmutableMap<AWSMetric, MetricTrait> AWSMetricTraits = ImmutableMap.<AWSMetric, MetricTrait>builder()
            .put(AWSMetric.LAMBDA_INVOCATIONS, MetricTrait.builder()
                    .namespace(LAMBDA_NAMESPACE)
                    .metricName(LAMBDA_INVOCATIONS)
                    .dimensionName("FunctionName")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.NAT_GATEWAY_ACTIVE_CONNECTIONS, MetricTrait.builder()
                    .namespace(NAT_GATEWAY_NAMESPACE)
                    .metricName(GATEWAY_ACTIVE_CONNECTIONS)
                    .dimensionName("NatGatewayId")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.EC2_INSTANCE_CPU_UTILIZATION, MetricTrait.builder()
                    .namespace(EC2_NAMESPACE)
                    .metricName(CPU_UTILIZATION)
                    .dimensionName("InstanceId")
                    .unit(StandardUnit.PERCENT).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.APPLICATION_ELB_REQUEST_COUNT, MetricTrait.builder()
                    .namespace(APPLICATION_ELB_NAMESPACE)
                    .metricName(REQUEST_COUNT)
                    .dimensionName("LoadBalancer")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.NETWORK_ELB_NEW_FLOW_COUNT, MetricTrait.builder()
                    .namespace(NETWORK_ELB_NAMESPACE)
                    .metricName(NEW_FLOW_COUNT)
                    .dimensionName("LoadBalancer")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.KINESIS_GET_RECORDS_RECORDS, MetricTrait.builder()
                    .namespace(KINESIS_NAMESPACE)
                    .metricName(GET_RECORDS_RECORDS)
                    .dimensionName("StreamName")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.RDS_CLUSTER_DB_CONNECTIONS, MetricTrait.builder()
                    .namespace(RDS_NAMESPACE)
                    .metricName(DB_CONNECTIONS)
                    .dimensionName("DBClusterIdentifier")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.REDSHIFT_SERVERLESS_COMPUTE_SECONDS, MetricTrait.builder()
                    .namespace(REDSHIFT_SERVERLESS_NAMESPACE)
                    .metricName(COMPUTE_SECONDS)
                    .dimensionName("Workgroup")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.REDSHIFT_CLUSTER_DATABASE_CONNECTIONS, MetricTrait.builder()
                    .namespace(REDSHIFT_NAMESPACE)
                    .metricName(DATABASE_CONNECTIONS)
                    .dimensionName("ClusterIdentifier")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.REDSHIFT_CLUSTER_CPU_UTILIZATION, MetricTrait.builder()
                    .namespace(REDSHIFT_NAMESPACE)
                    .metricName(CPU_UTILIZATION)
                    .dimensionName("ClusterIdentifier")
                    .unit(StandardUnit.PERCENT).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.CLOUDFRONT_DISTRIBUTION_REQUESTS, MetricTrait.builder()
                    .namespace(CLOUDFRONT_NAMESPACE)
                    .metricName(REQUESTS)
                    .dimensionName("DistributionId")
                    .unit(StandardUnit.NONE).statistic(Statistic.SUM).build())
            .put(AWSMetric.S3_BUCKET_SIZE_BYTES, MetricTrait.builder()
                    .namespace(S3_NAMESPACE)
                    .metricName(BUCKET_SIZE_BYTES)
                    .dimensionName("BucketName")
                    .unit(StandardUnit.BYTES).statistic(Statistic.SUM).build())
            .put(AWSMetric.ELASTICACHE_SHARD_CPU_UTILIZATION, MetricTrait.builder()
                    .namespace(ELASTICACHE_NAMESPACE)
                    .metricName(ENGINE_CPU_UTILIZATION)
                    .dimensionName("ReplicationGroupId")
                    .unit(StandardUnit.PERCENT).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.ELASTICACHE_SHARD_MEMORY, MetricTrait.builder()
                    .namespace(ELASTICACHE_NAMESPACE)
                    .metricName(ELASTICACHE_MEMORY_METRIC)
                    .dimensionName("ReplicationGroupId")
                    .unit(StandardUnit.PERCENT).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.ELASTICACHE_CLUSTER_NEW_CONNECTIONS, MetricTrait.builder()
                    .namespace(ELASTICACHE_NAMESPACE)
                    .metricName(NEW_CONNECTIONS)
                    .dimensionName("CacheClusterId")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.EFS_CLIENT_CONNECTIONS, MetricTrait.builder()
                    .namespace(EFS_NAMESPACE)
                    .metricName(CLIENT_CONNECTIONS)
                    .dimensionName("FileSystemId")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.FSX_USED_STORAGE_CAPACITY, MetricTrait.builder()
                    .namespace(FSX_NAMESPACE)
                    .metricName(USED_STORAGE_CAPACITY)
                    .dimensionName("FileSystemId")
                    .unit(StandardUnit.BYTES).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.FSX_STORAGE_USED, MetricTrait.builder()
                    .namespace(FSX_NAMESPACE)
                    .metricName(STORAGE_USED)
                    .dimensionName("FileSystemId")
                    .unit(StandardUnit.BYTES).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.FSX_FREE_DATA_STORAGE_CAPACITY, MetricTrait.builder()
                    .namespace(FSX_NAMESPACE)
                    .metricName(FREE_DATA_STORAGE_CAPACITY)
                    .dimensionName("FileSystemId")
                    .unit(StandardUnit.BYTES).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.FSX_FREE_STORAGE_CAPACITY, MetricTrait.builder()
                    .namespace(FSX_NAMESPACE)
                    .metricName(FREE_STORAGE_CAPACITY)
                    .dimensionName("FileSystemId")
                    .unit(StandardUnit.BYTES).statistic(Statistic.AVERAGE).build())
            .put(AWSMetric.APP_STREAM_CAPACITY_UTILIZATION, MetricTrait.builder()
                    .namespace(APP_STREAM_NAMESPACE)
                    .metricName(CAPACITY_UTILIZATION)
                    .dimensionName("Fleet")
                    .unit(StandardUnit.PERCENT).statistic(Statistic.MAXIMUM).build())
            .put(AWSMetric.CLOUDSEARCH_SUCCESSFUL_REQUESTS, MetricTrait.builder()
                    .namespace(CLOUDSEARCH_NAMESPACE)
                    .metricName(SUCCESSFUL_REQUESTS)
                    .dimensionName("DomainName")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.KINESIS_ZEPPELIN_SERVER_UPTIME, MetricTrait.builder()
                    .namespace(KINESIS_ANALYTICS_NAMESPACE)
                    .metricName(ZEPPELIN_SERVER_UPTIME)
                    .dimensionName("Application")
                    .unit(StandardUnit.NONE).statistic(Statistic.SUM).build())
            .put(AWSMetric.RDS_INSTANCE_DB_CONNECTIONS, MetricTrait.builder()
                    .namespace(RDS_NAMESPACE)
                    .metricName(DB_CONNECTIONS)
                    .dimensionName("DBInstanceIdentifier")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.RDS_PROXY_DB_CONNECTIONS, MetricTrait.builder()
                    .namespace(RDS_NAMESPACE)
                    .metricName(DB_CONNECTIONS)
                    .dimensionName("ProxyName")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .put(AWSMetric.BILLING_ESTIMATED_CHARGES, MetricTrait.builder()
                    .namespace(BILLING_NAMESPACE)
                    .metricName(ESTIMATED_CHARGES)
                    .dimensionName("ServiceName")
                    .unit(StandardUnit.NONE).statistic(Statistic.SUM).build())
            .put(AWSMetric.MEMORY_DB_CLUSTER_NEW_CONNECTIONS, MetricTrait.builder()
                    .namespace(MEMORY_DB_NAMESPACE)
                    .metricName(NEW_CONNECTIONS)
                    .dimensionName("ClusterName")
                    .unit(StandardUnit.COUNT).statistic(Statistic.SUM).build())
            .build();
}
