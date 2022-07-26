package com.cloudSavvy.dagger;

import com.cloudSavvy.aws.appstream.AppStreamAccessor;
import com.cloudSavvy.aws.autoscaling.AutoScalingAccessor;
import com.cloudSavvy.aws.cloudfront.CloudFrontAccessor;
import com.cloudSavvy.aws.cloudsearch.CloudSearchAccessor;
import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.cloudwatch.CloudWatchLogAccessor;
import com.cloudSavvy.aws.dynamodb.DynamoDbAccessor;
import com.cloudSavvy.aws.ec2.EC2Accessor;
import com.cloudSavvy.aws.ecs.ECSAccessor;
import com.cloudSavvy.aws.eks.EKSAccessor;
import com.cloudSavvy.aws.elasticache.ElastiCacheAccessor;
import com.cloudSavvy.aws.eventbridge.EventBridgeAccessor;
import com.cloudSavvy.aws.fsx.FSxAccessor;
import com.cloudSavvy.aws.glue.GlueAccessor;
import com.cloudSavvy.aws.kinesis.KinesisAccessor;
import com.cloudSavvy.aws.lambda.LambdaAccessor;
import com.cloudSavvy.aws.loadbalancer.LoadBalancerAccessor;
import com.cloudSavvy.aws.metric.BillingEstimatedChargesDimension;
import com.cloudSavvy.aws.metric.CloudSearchClientDimension;
import com.cloudSavvy.aws.metric.DefaultMetricDimension;
import com.cloudSavvy.aws.metric.ElastiCacheShardMetricDimension;
import com.cloudSavvy.aws.metric.MetricDimensionBuilderFactory;
import com.cloudSavvy.aws.rds.RDSAccessor;
import com.cloudSavvy.aws.redshift.RedshiftAccessor;
import com.cloudSavvy.aws.s3.S3Accessor;
import com.cloudSavvy.aws.sagemaker.SageMakerAccessor;
import com.cloudSavvy.aws.transfer.AWSTransferAccessor;
import com.cloudSavvy.aws.apigateway.ApiGatewayAccessor;
import com.cloudSavvy.aws.efs.EFSAccessor;
import com.cloudSavvy.aws.lightsail.LightsailAccessor;
import com.cloudSavvy.aws.memorydb.MemoryDbAccessor;
import com.cloudSavvy.aws.secretsmanager.SecretsManagerAccessor;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClient;
import software.amazon.awssdk.services.appstream.AppStreamClient;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudsearch.CloudSearchClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.fsx.FSxClient;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2Client;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lightsail.LightsailClient;
import software.amazon.awssdk.services.memorydb.MemoryDbClient;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshiftserverless.RedshiftServerlessClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.transfer.TransferClient;

import javax.inject.Singleton;

@Module
public class AWSAccessorModule {
    @Provides
    @Singleton
    public S3Accessor provideS3Accessor(final S3Client s3Client) {
        return new S3Accessor(s3Client);
    }

    @Provides
    @Singleton
    public DynamoDbAccessor provideDynamoDbAccessor(final DynamoDbClient dynamoDbClient) {
        return new DynamoDbAccessor(dynamoDbClient);
    }

    @Provides
    @Singleton
    public CloudWatchAccessor provideCloudWatchAccessor(final CloudWatchClient cloudWatchClient,
                                                        final MetricDimensionBuilderFactory dimensionBuilderFactory) {
        return new CloudWatchAccessor(cloudWatchClient, dimensionBuilderFactory);
    }

    @Provides
    @Singleton
    public MetricDimensionBuilderFactory provideMetricDimensionBuilderFactory() {
        DefaultMetricDimension defaultBuilder = new DefaultMetricDimension();
        ElastiCacheShardMetricDimension shardMetricDimension = new ElastiCacheShardMetricDimension();
        CloudSearchClientDimension clientDimension = new CloudSearchClientDimension();
        BillingEstimatedChargesDimension billingDimension = new BillingEstimatedChargesDimension();
        return new MetricDimensionBuilderFactory(defaultBuilder,
                shardMetricDimension, clientDimension, billingDimension);
    }

    @Provides
    @Singleton
    public AutoScalingAccessor provideAutoScalingAccessor(final ApplicationAutoScalingClient autoScalingClient) {
        return new AutoScalingAccessor(autoScalingClient);
    }

    @Provides
    @Singleton
    public LambdaAccessor provideLambdaAccessor(final LambdaClient lambdaClient) {
        return new LambdaAccessor(lambdaClient);
    }

    @Provides
    @Singleton
    public LoadBalancerAccessor provideLoadBalancerAccessor(final ElasticLoadBalancingV2Client loadBalancingV2Client,
                                                            final ElasticLoadBalancingClient loadBalancingClient) {
        return new LoadBalancerAccessor(loadBalancingV2Client, loadBalancingClient);
    }

    @Provides
    @Singleton
    public EC2Accessor provideEC2Accessor(final Ec2Client ec2Client) {
        return new EC2Accessor(ec2Client);
    }

    @Provides
    @Singleton
    public KinesisAccessor provideKinesisAccessor(final KinesisClient kinesisClient,
                                                  final KinesisAnalyticsV2Client kinesisAnalyticsV2Client) {
        return new KinesisAccessor(kinesisClient, kinesisAnalyticsV2Client);
    }

    @Provides
    @Singleton
    public CloudWatchLogAccessor provideCloudWatchLogAccessor(final CloudWatchLogsClient logsClient) {
        return new CloudWatchLogAccessor(logsClient);
    }

    @Provides
    @Singleton
    public RDSAccessor provideRDSAccessor(final RdsClient rdsClient) {
        return new RDSAccessor(rdsClient);
    }

    @Provides
    @Singleton
    public RedshiftAccessor provideRedshiftAccessor(final RedshiftServerlessClient redshiftServerlessClient,
                                                    final RedshiftClient redshiftClient) {
        return new RedshiftAccessor(redshiftServerlessClient, redshiftClient);
    }

    @Provides
    @Singleton
    public CloudFrontAccessor provideCloudFrontAccessor(final CloudFrontClient cloudFrontClient) {
        return new CloudFrontAccessor(cloudFrontClient);
    }

    @Provides
    @Singleton
    public ElastiCacheAccessor provideElastiCacheAccessor(final ElastiCacheClient elastiCacheClient) {
        return new ElastiCacheAccessor(elastiCacheClient);
    }

    @Provides
    @Singleton
    public EventBridgeAccessor provideEventBridgeAccessor(final EventBridgeClient eventBridgeClient) {
        return new EventBridgeAccessor(eventBridgeClient);
    }

    @Provides
    @Singleton
    public SageMakerAccessor provideSageMakerAccessor(final SageMakerClient sageMakerClient) {
        return new SageMakerAccessor(sageMakerClient);
    }

    @Provides
    @Singleton
    public EFSAccessor provideEFSAccessor(final EfsClient efsClient) {
        return new EFSAccessor(efsClient);
    }

    @Provides
    @Singleton
    public AWSTransferAccessor provideAWSTransferAccessor(final TransferClient transferClient) {
        return new AWSTransferAccessor(transferClient);
    }

    @Provides
    @Singleton
    public FSxAccessor provideFSxAccessor(final FSxClient fsxClient) {
        return new FSxAccessor(fsxClient);
    }

    @Provides
    @Singleton
    public GlueAccessor provideGlueAccessor(final GlueClient glueClient) {
        return new GlueAccessor(glueClient);
    }

    @Provides
    @Singleton
    public AppStreamAccessor provideAppStreamAccessor(final AppStreamClient appStreamClient) {
        return new AppStreamAccessor(appStreamClient);
    }

    @Provides
    @Singleton
    public EKSAccessor provideEKSAccessor(final EksClient eksClient) {
        return new EKSAccessor(eksClient);
    }

    @Provides
    @Singleton
    public LightsailAccessor provideLightsailAccessor(final LightsailClient lightsailClient) {
        return new LightsailAccessor(lightsailClient);
    }

    @Provides
    @Singleton
    public CloudSearchAccessor provideCloudSearchAccessor(final CloudSearchClient cloudSearchClient) {
        return new CloudSearchAccessor(cloudSearchClient);
    }

    @Provides
    @Singleton
    public ECSAccessor provideECSAccessor(final EcsClient ecsClient) {
        return new ECSAccessor(ecsClient);
    }

    @Provides
    @Singleton
    public SecretsManagerAccessor provideSecretsManagerAccessor(final SecretsManagerClient client) {
        return new SecretsManagerAccessor(client);
    }

    @Provides
    @Singleton
    public ApiGatewayAccessor provideApiGatewayAccessor(final ApiGatewayV2Client apiGatewayV2Client,
                                                        final ApiGatewayClient apiGatewayClient) {
        return new ApiGatewayAccessor(apiGatewayV2Client, apiGatewayClient);
    }

    @Provides
    @Singleton
    public MemoryDbAccessor provideMemoryDbAccessor(final MemoryDbClient memoryDbClient) {
        return new MemoryDbAccessor(memoryDbClient);
    }
}