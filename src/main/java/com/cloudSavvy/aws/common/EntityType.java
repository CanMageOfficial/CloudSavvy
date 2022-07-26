package com.cloudSavvy.aws.common;

import lombok.Getter;

public enum EntityType {
    S3_BUCKET("S3 Bucket"),
    DynamoDB_TABLE("DynamoDB Table"),
    LAMBDA_FUNCTION("Lambda Function"),
    ELASTIC_IP_ADDRESS("Elastic IP Address"),
    NAT_GATEWAY("NAT Gateway"),

    EC2_INSTANCE("EC2 Instance"),
    EBS_VOLUME("EBS Volume"),
    LOAD_BALANCER("Load Balancer"),
    EC2_TARGET_GROUP("EC2 Target Group"),
    KINESIS_DATA_STREAM("Kinesis Data Stream"),
    KINESIS_STUDIO_NOTEBOOK("Kinesis Studio Notebook"),
    CLOUDWATCH_LOG_GROUP("CloudWatch Log Group"),
    RDS_DB_CLUSTER("RDS Database Cluster"),
    RDS_DB_INSTANCE("RDS Database Instance"),
    REDSHIFT_SERVERLESS_WORKGROUP("Amazon Redshift Serverless Workgroup"),
    REDSHIFT_CLUSTER("Amazon Redshift Cluster"),
    CLOUDFRONT_DISTRIBUTION("CloudFront Distribution"),
    ELASTICACHE_REDIS_CLUSTER("ElastiCache Redis Cluster"),
    EVENTBRIDGE_ARCHIVE("Amazon EventBridge Archive"),
    SAGEMAKER_NOTEBOOK_INSTANCE("Amazon SageMaker Notebook Instance"),
    ELASTICACHE_MEMCACHED_CLUSTER("ElastiCache Memcached Cluster"),
    EFS_FILE_SYSTEMS("EFS File systems"),
    SAGEMAKER_ENDPOINT("Amazon SageMaker Endpoint"),
    STACK("CloudFormation Stack"),
    AWS_TRANSFER_SERVER("AWS Transfer Server"),
    FSX_FILE_SYSTEM("FSx File System"),
    GLUE_JOB("AWS Glue Job"),
    GLUE_DEV_ENDPOINT("AWS Glue Dev Endpoint"),
    AppStream_Fleet("AppStream 2.0 Fleet"),
    EKS_CLUSTER("Elastic Kubernetes Service Cluster"),
    LightSail_INSTANCE("Amazon Lightsail Instance"),
    CLOUDSEARCH_DOMAIN("CloudSearch Domain"),
    RDS_DB_PROXY("RDS Proxy"),
    LightSail_CONTAINER("Amazon Lightsail Container"),
    LightSail_DATABASE("Amazon Lightsail Database"),
    LightSail_STATIC_IP("Amazon Lightsail Static IP"),
    LightSail_LOAD_BALANCER("Amazon Lightsail Load Balancer"),
    LightSail_DISK("Amazon Lightsail Disk"),
    ECS_CLUSTER("Elastic Container Service Cluster"),
    SECRETS_MANAGER_SECRET("Secret Manager Secret"),
    ECS_CLUSTER_SERVICE("ECS Cluster Service", true),
    APIGateway_METHOD("API Gateway Method"),
    APIGateway_API("API Gateway Api"),
    MEMORY_DB_CLUSTER("Amazon MemoryDB Cluster"),

    BILLING("Billing");

    private final String text;

    @Getter
    private final boolean isContainer;

    EntityType(final String text) {
        this.text = text;
        this.isContainer = false;
    }

    EntityType(final String text, boolean isContainer) {
        this.text = text;
        this.isContainer = isContainer;
    }

    public String toString() {
        return text;
    }
}
