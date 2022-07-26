package com.cloudSavvy.aws.common;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.regions.Region;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EntityLinks {

    public static final ImmutableMap<EntityType, String> ENTITY_LINK_TEMPLATES = ImmutableMap.<EntityType, String>builder()
            .put(EntityType.DynamoDB_TABLE, "https://%1$s.console.aws.amazon.com/dynamodbv2/home?region=%1$s#table?name=%2$s")
            .put(EntityType.ELASTIC_IP_ADDRESS, "https://%1$s.console.aws.amazon.com/vpc/home?region=%1$s#ElasticIpDetails:AllocationId=%2$s")
            .put(EntityType.LAMBDA_FUNCTION, "https://%1$s.console.aws.amazon.com/lambda/home?region=%1$s#/functions/%2$s")
            .put(EntityType.EC2_INSTANCE, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#InstanceDetails:instanceId=%2$s")
            .put(EntityType.EBS_VOLUME, "https://%1$s.console.aws.amazon.com/ec2/v2/home?region=%1$s#VolumeDetails:volumeId=%2$s")
            .put(EntityType.LOAD_BALANCER, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#LoadBalancers:search=%2$s")
            .put(EntityType.EC2_TARGET_GROUP, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#TargetGroups:search=%2$s")
            .put(EntityType.KINESIS_DATA_STREAM, "https://%1$s.console.aws.amazon.com/kinesis/home?region=%1$s#/streams/details/%2$s")
            .put(EntityType.NAT_GATEWAY, "https://%1$s.console.aws.amazon.com/vpc/home?region=%1$s#NatGatewayDetails:natGatewayId=%2$s")
            .put(EntityType.CLOUDWATCH_LOG_GROUP, "https://%1$s.console.aws.amazon.com/cloudwatch/home?region=%1$s#logsV2:log-groups/log-group/%2$s")
            .put(EntityType.RDS_DB_CLUSTER, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#database:id=%2$s;is-cluster=true")
            .put(EntityType.RDS_DB_INSTANCE, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#database:id=%2$s;is-cluster=false")
            .put(EntityType.REDSHIFT_SERVERLESS_WORKGROUP, "https://%1$s.console.aws.amazon.com/redshiftv2/home?region=%1$s#serverless-workgroup?workgroup=%2$s")
            .put(EntityType.REDSHIFT_CLUSTER, "https://%1$s.console.aws.amazon.com/redshiftv2/home?region=%1$s#cluster-details?cluster=%2$s")
            .put(EntityType.CLOUDFRONT_DISTRIBUTION, "https://%1$s.console.aws.amazon.com/cloudfront/v3/home?region=%1$s#/distributions/%2$s")
            .put(EntityType.S3_BUCKET, "https://s3.console.aws.amazon.com/s3/buckets/%2$s?region=%1$s&tab=objects")
            .put(EntityType.EVENTBRIDGE_ARCHIVE, "https://%1$s.console.aws.amazon.com/events/home?region=%1$s#/archive/%2$s")
            .put(EntityType.SAGEMAKER_NOTEBOOK_INSTANCE, "https://%1$s.console.aws.amazon.com/sagemaker/home?region=%1$s#/notebook-instances/%2$s")
            .put(EntityType.ELASTICACHE_REDIS_CLUSTER, "https://%1$s.console.aws.amazon.com/elasticache/home?region=%1$s#/redis/%2$s")
            .put(EntityType.KINESIS_STUDIO_NOTEBOOK, "https://%1$s.console.aws.amazon.com/kinesisanalytics/home?region=%1$s#/notebook/%2$s/details/monitoring")
            .put(EntityType.ELASTICACHE_MEMCACHED_CLUSTER, "https://%1$s.console.aws.amazon.com/elasticache/home?region=%1$s#/memcached/%2$s")
            .put(EntityType.EFS_FILE_SYSTEMS, "https://%1$s.console.aws.amazon.com/efs/home?region=%1$s#/file-systems/%2$s")
            .put(EntityType.SAGEMAKER_ENDPOINT, "https://%1$s.console.aws.amazon.com/sagemaker/home?region=%1$s#/endpoints/%2$s")
            .put(EntityType.STACK, "https://%1$s.console.aws.amazon.com/cloudformation/home?region=%1$s#/stacks?filteringStatus=active&filteringText=%2$s&viewNested=true&hideStacks=false&stackId=")
            .put(EntityType.AWS_TRANSFER_SERVER, "https://%1$s.console.aws.amazon.com/transfer?region=%1$s#/servers/%2$s")
            .put(EntityType.FSX_FILE_SYSTEM, "https://%1$s.console.aws.amazon.com/fsx/home?region=%1$s#file-system-details/%2$s")
            .put(EntityType.GLUE_JOB, "https://%1$s.console.aws.amazon.com/gluestudio/home?region=%1$s#/editor/job/%2$s/graph")
            .put(EntityType.GLUE_DEV_ENDPOINT, "https://%1$s.console.aws.amazon.com/glue/home?region=%1$s#devEndpoint:name=%2$s")
            .put(EntityType.AppStream_Fleet, "https://%1$s.console.aws.amazon.com/appstream2/home?region=%1$s#/fleets/%2$s")
            .put(EntityType.EKS_CLUSTER, "https://%1$s.console.aws.amazon.com/eks/home?region=%1$s#/clusters/%2$s")
            .put(EntityType.LightSail_INSTANCE, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/instances/%2$s/connect")
            .put(EntityType.CLOUDSEARCH_DOMAIN, "https://%1$s.console.aws.amazon.com/cloudsearch/home?region=%1$s#domainDashboard,%2$s")
            .put(EntityType.RDS_DB_PROXY, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#proxy:id=%2$s")
            .put(EntityType.LightSail_CONTAINER, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/container-services/%2$s/deployments")
            .put(EntityType.LightSail_DATABASE, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/databases/%2$s/connect")
            .put(EntityType.LightSail_STATIC_IP, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/%2$s/StaticIp-1")
            .put(EntityType.LightSail_LOAD_BALANCER, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/load-balancers/%2$s/target-instances?#")
            .put(EntityType.LightSail_DISK, "https://lightsail.aws.amazon.com/ls/webapp/%1$s/disks/%2$s")
            .put(EntityType.SECRETS_MANAGER_SECRET, "https://%1$s.console.aws.amazon.com/secretsmanager/secret?name=%2$s&region=%1$s")
            .put(EntityType.ECS_CLUSTER, "https://%1$s.console.aws.amazon.com/ecs/home?region=%1$s#/clusters/%2$s/services")
            .put(EntityType.ECS_CLUSTER_SERVICE, "https://%1$s.console.aws.amazon.com/ecs/home?region=%1$s#/clusters/%2$s/services/%3$s/details")
            .put(EntityType.APIGateway_API, "https://%1$s.console.aws.amazon.com/apigateway/home?region=%1$s#/apis/%2$s/resources/")
            .put(EntityType.APIGateway_METHOD, "https://%1$s.console.aws.amazon.com/apigateway/home?region=%1$s#/apis/%2$s")
            .put(EntityType.MEMORY_DB_CLUSTER, "https://%1$s.console.aws.amazon.com/memorydb/home?region=%1$s#/clusters/%2$s")
            .put(EntityType.BILLING, "https://%1$s.console.aws.amazon.com/billing/home?region=%1$s#/bills")
            .build();

    public static final ImmutableMap<EntityType, String> SERVICE_LINK_TEMPLATES = ImmutableMap.<EntityType, String>builder()
            .put(EntityType.DynamoDB_TABLE, "https://%1$s.console.aws.amazon.com/dynamodbv2/home?region=%1$s#table")
            .put(EntityType.ELASTIC_IP_ADDRESS, "https://%1$s.console.aws.amazon.com/vpc/home?region=%1$s#Addresses:")
            .put(EntityType.LAMBDA_FUNCTION, "https://%1$s.console.aws.amazon.com/lambda/home?region=%1$s")
            .put(EntityType.EC2_INSTANCE, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#Instances:")
            .put(EntityType.EBS_VOLUME, "https://%1$s.console.aws.amazon.com/ec2/v2/home?region=%1$s#Volumes:")
            .put(EntityType.LOAD_BALANCER, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#LoadBalancers")
            .put(EntityType.EC2_TARGET_GROUP, "https://%1$s.console.aws.amazon.com/ec2/home?region=%1$s#TargetGroups:")
            .put(EntityType.KINESIS_DATA_STREAM, "https://%1$s.console.aws.amazon.com/kinesis/home?region=%1$s#/streams/list")
            .put(EntityType.NAT_GATEWAY, "https://%1$s.console.aws.amazon.com/vpc/home?region=%1$s#NatGateways:")
            .put(EntityType.RDS_DB_CLUSTER, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#databases:")
            .put(EntityType.RDS_DB_INSTANCE, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#databases:")
            .put(EntityType.REDSHIFT_SERVERLESS_WORKGROUP, "https://%1$s.console.aws.amazon.com/redshiftv2/home?region=%1$s#serverless-workgroups")
            .put(EntityType.REDSHIFT_CLUSTER, "https://%1$s.console.aws.amazon.com/redshiftv2/home?region=%1$s#clusters")
            .put(EntityType.CLOUDFRONT_DISTRIBUTION, "https://%1$s.console.aws.amazon.com/cloudfront/v3/home?region=%1$s#/distributions")
            .put(EntityType.S3_BUCKET, "https://s3.console.aws.amazon.com/s3/buckets?region=%1$s#")
            .put(EntityType.EVENTBRIDGE_ARCHIVE, "https://%1$s.console.aws.amazon.com/events/home?region=%1$s#/archives")
            .put(EntityType.SAGEMAKER_NOTEBOOK_INSTANCE, "https://%1$s.console.aws.amazon.com/sagemaker/home?region=%1$s#/notebook-instances")
            .put(EntityType.ELASTICACHE_REDIS_CLUSTER, "https://%1$s.console.aws.amazon.com/elasticache/home?region=%1$s#/redis")
            .put(EntityType.KINESIS_STUDIO_NOTEBOOK, "https://%1$s.console.aws.amazon.com/kinesisanalytics/home?region=%1$s#/list/notebooks")
            .put(EntityType.CLOUDWATCH_LOG_GROUP, "https://%1$s.console.aws.amazon.com/cloudwatch/home?region=%1$s#logsV2:log-groups")
            .put(EntityType.ELASTICACHE_MEMCACHED_CLUSTER, "https://%1$s.console.aws.amazon.com/elasticache/home?region=%1$s#/memcached")
            .put(EntityType.EFS_FILE_SYSTEMS, "https://%1$s.console.aws.amazon.com/efs/home?region=%1$s#/file-systems")
            .put(EntityType.SAGEMAKER_ENDPOINT, "https://%1$s.console.aws.amazon.com/sagemaker/home?region=%1$s#/endpoints")
            .put(EntityType.STACK, "https://%1$s.console.aws.amazon.com/cloudformation/home?region=%1$s#/stacks?filteringStatus=active&filteringText=&viewNested=true&hideStacks=false")
            .put(EntityType.AWS_TRANSFER_SERVER, "https://%1$s.console.aws.amazon.com/transfer?region=%1$s#/servers")
            .put(EntityType.FSX_FILE_SYSTEM, "https://%1$s.console.aws.amazon.com/fsx/home?region=%1$s#file-systems")
            .put(EntityType.GLUE_JOB, "https://%1$s.console.aws.amazon.com/gluestudio/home?region=%1$s#/jobs")
            .put(EntityType.GLUE_DEV_ENDPOINT, "https://%1$s.console.aws.amazon.com/glue/home?region=%1$s#etl:tab=devEndpoints")
            .put(EntityType.AppStream_Fleet, "https://%1$s.console.aws.amazon.com/appstream2/home?region=%1$s#/fleets")
            .put(EntityType.EKS_CLUSTER, "https://%1$s.console.aws.amazon.com/eks/home?region=%1$s#/clusters")
            .put(EntityType.LightSail_INSTANCE, "https://lightsail.aws.amazon.com/ls/webapp/home/instances")
            .put(EntityType.CLOUDSEARCH_DOMAIN, "https://%1$s.console.aws.amazon.com/cloudsearch/home?region=%1$s#")
            .put(EntityType.RDS_DB_PROXY, "https://%1$s.console.aws.amazon.com/rds/home?region=%1$s#proxies:")
            .put(EntityType.LightSail_CONTAINER, "https://lightsail.aws.amazon.com/ls/webapp/home/containers")
            .put(EntityType.LightSail_DATABASE, "https://lightsail.aws.amazon.com/ls/webapp/home/databases")
            .put(EntityType.LightSail_STATIC_IP, "https://lightsail.aws.amazon.com/ls/webapp/home/networking")
            .put(EntityType.LightSail_LOAD_BALANCER, "https://lightsail.aws.amazon.com/ls/webapp/home/networking")
            .put(EntityType.LightSail_DISK, "https://lightsail.aws.amazon.com/ls/webapp/home/storage")
            .put(EntityType.SECRETS_MANAGER_SECRET, "https://%1$s.console.aws.amazon.com/secretsmanager/listsecrets?region=%1$s")
            .put(EntityType.ECS_CLUSTER, "https://%1$s.console.aws.amazon.com/ecs/home?region=%1$s#/clusters")
            .put(EntityType.ECS_CLUSTER_SERVICE, "https://%1$s.console.aws.amazon.com/ecs/home?region=%1$s#/clusters")
            .put(EntityType.APIGateway_API, "https://%1$s.console.aws.amazon.com/apigateway/main/apis?region=%1$s")
            .put(EntityType.APIGateway_METHOD, "https://%1$s.console.aws.amazon.com/apigateway/main/apis?region=%1$s")
            .put(EntityType.MEMORY_DB_CLUSTER, "https://%1$s.console.aws.amazon.com/memorydb/home?region=%1$s#/clusters")
            .put(EntityType.BILLING, "https://%1$s.console.aws.amazon.com/billing/home?region=%1$s#/bills")
            .build();

    private static final String BUCKET_OBJECT_PREFIX_TEMPLATE = "https://s3.console.aws.amazon.com/s3/object/%s?region=%s&prefix=%s";

    public static String getEntityLink(Region region, EntityType entityType, String resourceId) {
        if (entityType.isContainer()) {
            throw new IllegalArgumentException("getEntityLink should not be called for container entities: " + entityType);
        }

        String template = EntityLinks.ENTITY_LINK_TEMPLATES.get(entityType);
        String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8);
        return String.format(Objects.requireNonNull(template), region, encodedResourceId);
    }

    public static String getEntityLink(Region region, EntityType entityType, String containerId, String resourceId) {
        if (!entityType.isContainer()) {
            throw new IllegalArgumentException("getEntityLink should only be called for container entities: " + entityType);
        }

        String template = EntityLinks.ENTITY_LINK_TEMPLATES.get(entityType);
        String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8);
        String encodedContainerId = URLEncoder.encode(containerId, StandardCharsets.UTF_8);
        return String.format(Objects.requireNonNull(template), region, encodedContainerId, encodedResourceId);
    }

    public static String getServiceLink(final Region region, final EntityType entityType) {
        String template = EntityLinks.SERVICE_LINK_TEMPLATES.get(entityType);
        return String.format(Objects.requireNonNull(template), region);
    }

    public static String getS3ObjectPrefixLink(final String bucket, final String region, final String objectPrefix) {
        return String.format(BUCKET_OBJECT_PREFIX_TEMPLATE, bucket, region, objectPrefix);
    }
}
