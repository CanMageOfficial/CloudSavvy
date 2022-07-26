package com.cloudSavvy.aws.common;

import lombok.Getter;

public enum IssueCode {
    TABLE_NO_READ_AUTO_SCALE("DynamoDb Table Read Auto Scaling is Off", IssueSeverity.HIGH),
    TABLE_NO_WRITE_AUTO_SCALE("DynamoDb Table Write Auto Scaling is Off", IssueSeverity.HIGH),
    LAMBDA_FUNCTION_PROVISIONED_BUT_NOT_USED("Lambda Function Is Provisioned But Not Used", IssueSeverity.HIGH),
    ELASTIC_IP_ADDRESS_IS_NOT_ASSOCIATED("Elastic IP Address Is Not Associated", IssueSeverity.HIGH),
    NAT_GATEWAY_NOT_USED("NatGateway Is Not Used"),
    EC2_INSTANCE_STOPPED("EC2 Instance Is Stopped", IssueSeverity.HIGH),
    EC2_INSTANCE_CPU_UTILIZATION_LOW("EC2 Instance Utilization Is Low"),
    EBS_VOLUME_UNATTACHED("EBS Volume Is Not Attached"),
    EBS_VOLUME_IN_ERROR("EBS Volume Is In Error"),
    EBS_VOLUME_STATUS_IMPAIRED("EBS Volume Is In Impaired Status", IssueSeverity.HIGH),
    APPLICATION_LOAD_BALANCER_NOT_USED("Application Load Balancer Is Not Used", IssueSeverity.HIGH),
    NETWORK_LOAD_BALANCER_NOT_USED("Network Load Balancer Is Not Used", IssueSeverity.HIGH),
    CLASSIC_LOAD_BALANCER_DEPRECATED("AWS Classic Load Balancer Will Be Deprecated", IssueSeverity.HIGH),
    TARGET_GROUP_HEALTH_CHECK_NOT_ENABLED("Health Check Is Not Enabled For Load Balancer Target Group"),
    TARGET_GROUP_HAS_UNHEALTHY_TARGETS("Load Balancer Target Group Has Unhealthy Targets"),
    KINESIS_DATA_STREAM_NOT_USED("Kinesis Data Stream Is Not Used"),
    KINESIS_DATA_STREAM_PROVISIONED_NOT_USED("Kinesis Data Stream Is Provisioned But Not Used", IssueSeverity.HIGH),
    CLOUDWATCH_LOG_GROUP_HAS_NO_RETENTION("CloudWatch Log Group Has No Retention", IssueSeverity.LOW),
    RDS_DB_CLUSTER_NOT_USED("RDS Database Cluster Is Not Used", IssueSeverity.HIGH),
    RDS_DB_CLUSTER_HAS_UNUSED_MEMBERS("RDS Database Cluster Has Unused Members"),
    RDS_DB_INSTANCE_NOT_USED("RDS Database Instance Is Not Used", IssueSeverity.HIGH),
    RDS_DB_INSTANCE_STORAGE_FULL("RDS DB Instance Has Storage-Full Status"),
    RDS_DB_INSTANCE_STOPPED("RDS DB Instance Is Stopped", IssueSeverity.HIGH),
    RDS_DB_INSTANCE_INCOMPATIBLE_PARAMETERS("RDS DB Instance Has Incompatible Parameters Status"),
    RDS_DB_INSTANCE_INCOMPATIBLE_OPTION_GROUP("RDS DB Instance Has Incompatible Option Group Status"),
    REDSHIFT_SERVERLESS_WORKGROUP_NOT_USED("Redshift Serverless Workgroup Is Not Used", IssueSeverity.LOW),
    REDSHIFT_CLUSTER_NOT_USED("Redshift Cluster Is Not Used"),
    Redshift_CLUSTER_STORAGE_FULL("Redshift Cluster's Status Is Storage-Full"),
    Redshift_CLUSTER_INCOMPATIBLE_NETWORK("Redshift Cluster's Status Is Incompatible Network"),
    Redshift_CLUSTER_INCOMPATIBLE_PARAMETERS("Redshift Cluster's Status Is Incompatible Parameters"),
    Redshift_CLUSTER_INCOMPATIBLE_HSM("Redshift Cluster's Status Is Incompatible-Hsm"),
    REDSHIFT_CLUSTER_UNDER_UTILIZED("Redshift Cluster Utilization Is Low"),
    CLOUDFRONT_DISTRIBUTION_WITH_CUSTOM_SSL("CloudFront Distribution Has Custom SSL Method"),
    EVENTBRIDGE_ARCHIVE_HAS_NO_RETENTION("EventBridge Archive Has No Retention", IssueSeverity.LOW),
    SAGEMAKER_NOTEBOOK_INSTANCE_NOT_USED("Sagemaker Notebook Instance Is Not Used", IssueSeverity.HIGH),
    ELASTICACHE_REDIS_CLUSTER_HAS_LOW_USAGE("ElastiCache Redis Cluster Has Low Usage", IssueSeverity.LOW),
    ELASTICACHE_MEMCACHED_CLUSTER_NOT_USED("ElastiCache Memcached Cluster Is Not Used", IssueSeverity.HIGH),
    EFS_FILE_SYSTEM_HAS_NO_MOUNT_TARGET("EFS File System Has No Mount Target"),
    EFS_FILE_SYSTEM_NOT_USED("EFS File System Is Not Used"),
    EFS_FILE_SYSTEM_HAS_NO_LIFECYCLE_POLICY("EFS File System Is Missing Lifecycle Policy"),
    EFS_FILE_SYSTEM_MISSING_TRANSITION_TO_IA("EFS File System Is Missing 'Transition into IA'"),
    EFS_FILE_SYSTEM_MISSING_TRANSITION_TO_PRIMARY_STORAGE("EFS File System Is Missing 'Transition Out Of IA'"),
    LAMBDA_FUNCTION_ARCH_CAN_BE_ARM64("Lambda Function Architecture Is Not Arm64", IssueSeverity.LOW),
    SAGEMAKER_ENDPOINT_NOT_USED("Sagemaker Endpoint Is Not Used"),
    PRIVATE_NAT_GATEWAY_DETECTED("Private NAT Gateway Found"),
    AWS_TRANSFER_SERVER_NOT_USED("AWS Transfer Server Is Not Used"),
    AWS_TRANSFER_SERVER_HAS_NO_USER("AWS Transfer Server Has No Users"),
    FSX_FILE_SYSTEM_HAS_LOW_USAGE("FSx File System Has Low Usage"),
    FSX_LUSTRE_FILE_SYSTEM_HAS_NO_COMPRESSION("FSx File System For Lustre Has No Compression"),
    APP_STREAM_FLEET_NOT_USED("AppStream 2.0 Fleet Is Not Used"),
    APP_STREAM_ALWAYS_ON_FLEET_LOW_USAGE("AppStream 2.0 Always-On Fleet Has Low Usage"),
    EKS_CLUSTER_HAS_NO_FARGATE_AND_NODEGROUP("EKS Cluster Does Not Have Compute Resource", IssueSeverity.HIGH),
    EKS_CLUSTER_HAS_FAILED_STATUS("EKS Cluster Has Failed Status"),
    GLUE_DEVELOPMENT_ENDPOINT_FOUND("Glue Dev Endpoints Are Used", IssueSeverity.HIGH),
    LIGHTSAIL_INSTANCE_HAS_LOW_USAGE("Lightsail Instance Has Low Usage"),
    LOAD_BALANCER_STATE_IS_ACTIVE_IMPAIRED("Load Balancer Is In Active Impaired State", IssueSeverity.HIGH),
    LOAD_BALANCER_STATE_IS_FAILED("Load Balancer Is In Failed State"),
    CLOUD_SEARCH_DOMAIN_NOT_USED("CloudSearch Domain Is Not Used"),
    KINESIS_STUDIO_NOTEBOOK_RUNNING_LONG("Kinesis Studio Notebook Is Running Long"),
    RDS_PROXY_HAS_NO_TARGET("RDS Proxy Has No Target", IssueSeverity.HIGH),
    RDS_DB_PROXY_NOT_USED("RDS Proxy Is Not Used"),
    S3_PUBLIC_BUCKET_HAS_NO_MONITORING("Public S3 Bucket Has No Monitoring"),
    S3_BUCKET_IS_GROWING_FAST("S3 Bucket Size Is Growing Fast", IssueSeverity.LOW),
    LIGHTSAIL_CONTAINER_IS_DISABLED("LightSail Container Is Disabled", IssueSeverity.HIGH),
    LIGHTSAIL_CONTAINER_IS_NOT_DEPLOYED("LightSail Container Is Not Deployed", IssueSeverity.HIGH),
    LIGHTSAIL_CONTAINER_HAS_LOW_USAGE("Lightsail Container Has Low Usage", IssueSeverity.LOW),
    LIGHTSAIL_DATABASE_IS_NOT_USED("Lightsail Container Database Is Not Used", IssueSeverity.HIGH),
    LIGHTSAIL_DATABASE_IS_STOPPED("Lightsail Container Database Is Stopped", IssueSeverity.HIGH),
    LIGHTSAIL_STATIC_IP_IS_NOT_ATTACHED("Lightsail Static IP Is Not Attached"),
    LIGHTSAIL_LOAD_BALANCER_IS_NOT_USED("Lightsail Load Balancer Is Not Used", IssueSeverity.HIGH),
    LIGHTSAIL_LOAD_BALANCER_IS_FAILED("Lightsail Load Balancer Is In Failed State"),
    LIGHTSAIL_LOAD_BALANCER_IS_ACTIVE_IMPAIRED("Lightsail Load Balancer Is In Active Impaired State"),
    LIGHTSAIL_LOAD_BALANCER_HAS_NO_TARGET("Lightsail Load Balancer Has No Target Instance", IssueSeverity.HIGH),
    LIGHTSAIL_LOAD_BALANCER_HAS_NO_HEALTHY_TARGET("Lightsail Load Balancer Has No Healthy Target", IssueSeverity.HIGH),
    LIGHTSAIL_LOAD_BALANCER_HAS_UNHEALTHY_TARGET("Lightsail Load Balancer Has Unhealthy Target"),
    LIGHTSAIL_LOAD_BALANCER_HAS_UNUSED_TARGET("Lightsail Load Balancer Has Unused Target"),
    LIGHTSAIL_LOAD_BALANCER_HAS_ONLY_ONE_TARGET("Lightsail Load Balancer Has Only One Target"),
    LIGHTSAIL_DISK_IS_NOT_ATTACHED("Lightsail Disk Is Not Attached"),
    SECRETS_MANAGER_UNUSED_SECRET("Secrets Manager Has Unused Secrets", IssueSeverity.LOW),
    ECS_CLUSTER_IN_FAILED_STATE("ECS Cluster Is In Failed Status"),
    ECS_CLUSTER_SERVICE_HAS_NO_TASK("ECS Cluster Service Has No Task"),
    ECS_CLUSTER_SERVICE_IS_NOT_RUNNING_ALL_TASKS("ECS Cluster Service Is Not Running All Tasks"),
    API_GATEWAY_API_METHOD_HAS_NO_SECURITY("API Gateway Method Has No Security Enabled", IssueSeverity.HIGH),
    AWS_NODEJS_CONNECTION_REUSE_ENABLED_MISSING("Lambda Function Is Missing Environment Variable", IssueSeverity.LOW),
    MEMORY_DB_CLUSTER_NOT_USED("Amazon MemoryDB for Redis Cluster Is Not Used", IssueSeverity.HIGH),
    PROVISIONED_KINESIS_DATA_STREAM_HAS_LOW_USAGE("Provisioned Kinesis Data Stream Has Low Usage"),
    S3_DEVELOPMENT_BUCKET_HAS_NO_LIFECYCLE_CONFIG("Development S3 Bucket Has No Lifecycle Configuration"),
    SERVICE_COST_HAS_SPIKE("Service cost has sudden increase.", IssueSeverity.HIGH),
    LAMBDA_RUNTIME_DEPRECATED("Lambda function runtime is deprecated", IssueSeverity.MEDIUM);

    private final String text;
    @Getter
    private final IssueSeverity issueSeverity;

    IssueCode(final String text, final IssueSeverity severity) {
        this.text = text;
        this.issueSeverity = severity;
    }

    IssueCode(final String text) {
        this.text = text;
        this.issueSeverity = IssueSeverity.MEDIUM;
    }

    public String toString() {
        return text != null ? text : this.name();
    }

}
