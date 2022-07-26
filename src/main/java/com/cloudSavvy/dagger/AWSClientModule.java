package com.cloudSavvy.dagger;

import com.cloudSavvy.common.run.RunMetadata;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.regions.Region;
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
import software.amazon.awssdk.services.sagemaker.SageMakerAsyncClient;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.transfer.TransferClient;

import javax.inject.Singleton;

@Module
public class AWSClientModule {

    @Provides
    @Singleton
    public S3Client provideS3Client(final AwsCredentialsProvider credentialsProvider,
                                    final Region region) {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .defaultsMode(DefaultsMode.CROSS_REGION)
                .build();
    }

    @Provides
    @Singleton
    public DynamoDbClient provideDynamoDbClient(final AwsCredentialsProvider credentialsProvider,
                                                final Region region) {
        return DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public CloudWatchClient provideCloudWatchClient(final AwsCredentialsProvider credentialsProvider,
                                                    final Region region) {
        return CloudWatchClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public ApplicationAutoScalingClient provideAutoScalingClient(final AwsCredentialsProvider credentialsProvider,
                                                                 final Region region) {
        return ApplicationAutoScalingClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public SageMakerAsyncClient provideSageMakerAsyncClient(final AwsCredentialsProvider credentialsProvider,
                                                            final Region region) {
        return SageMakerAsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public LambdaClient provideLambdaClient(final AwsCredentialsProvider credentialsProvider,
                                            final Region region) {
        return LambdaClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public ElasticLoadBalancingV2Client provideLoadBalancingV2Client(final AwsCredentialsProvider credentialsProvider,
                                                                     final Region region) {
        return ElasticLoadBalancingV2Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public ElasticLoadBalancingClient provideLoadBalancingClient(final AwsCredentialsProvider credentialsProvider,
                                                                 final Region region) {
        return ElasticLoadBalancingClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public Ec2Client provideEc2Client(final AwsCredentialsProvider credentialsProvider,
                                      final Region region) {
        return Ec2Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public KinesisClient provideKinesisClient(final AwsCredentialsProvider credentialsProvider,
                                              final Region region) {
        return KinesisClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public KinesisAnalyticsV2Client provideKinesisAnalyticsV2Client(AwsCredentialsProvider credentialsProvider,
                                                                    Region region) {
        return KinesisAnalyticsV2Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public CloudWatchLogsClient provideCloudWatchLogsClient(AwsCredentialsProvider credentialsProvider,
                                                            Region region) {
        return CloudWatchLogsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public RdsClient provideRdsClient(AwsCredentialsProvider credentialsProvider,
                                      Region region) {
        return RdsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public RedshiftServerlessClient provideRedshiftServerlessClient(AwsCredentialsProvider credentialsProvider,
                                                                    Region region) {
        return RedshiftServerlessClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public RedshiftClient provideRedshiftClient(AwsCredentialsProvider credentialsProvider,
                                                Region region) {
        return RedshiftClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public CloudFrontClient provideCloudFrontClient(AwsCredentialsProvider credentialsProvider) {
        return CloudFrontClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.AWS_GLOBAL)
                .build();
    }

    @Provides
    @Singleton
    public ElastiCacheClient provideElastiCacheClient(final AwsCredentialsProvider credentialsProvider,
                                                      final Region region) {
        return ElastiCacheClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public EventBridgeClient provideEventBridgeClient(AwsCredentialsProvider credentialsProvider,
                                                      Region region) {
        return EventBridgeClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public SageMakerClient provideSageMakerClient(AwsCredentialsProvider credentialsProvider,
                                                  Region region) {
        return SageMakerClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public EfsClient provideEfsClient(AwsCredentialsProvider credentialsProvider,
                                      Region region) {
        return EfsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public TransferClient provideTransferClient(AwsCredentialsProvider credentialsProvider,
                                                Region region) {
        return TransferClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public FSxClient provideFSxClient(AwsCredentialsProvider credentialsProvider,
                                      Region region) {
        return FSxClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public GlueClient provideGlueClient(AwsCredentialsProvider credentialsProvider,
                                        Region region) {
        return GlueClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public AppStreamClient provideAppStreamClient(AwsCredentialsProvider credentialsProvider,
                                                  Region region) {
        return AppStreamClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public EksClient provideEksClient(final AwsCredentialsProvider credentialsProvider,
                                      final Region region) {
        return EksClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public LightsailClient provideLightsailClient(final AwsCredentialsProvider credentialsProvider,
                                                  final Region region) {
        return LightsailClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public CloudSearchClient provideCloudSearchClient(final AwsCredentialsProvider credentialsProvider,
                                                      final Region region) {
        return CloudSearchClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public EcsClient provideEcsClient(final AwsCredentialsProvider credentialsProvider,
                                      final Region region) {
        return EcsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public SecretsManagerClient provideSecretsManagerClient(final AwsCredentialsProvider credentialsProvider,
                                                            final Region region) {
        return SecretsManagerClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public ApiGatewayV2Client provideApiGatewayV2Client(final AwsCredentialsProvider credentialsProvider,
                                                        final Region region) {
        return ApiGatewayV2Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public ApiGatewayClient provideApiGatewayClient(final AwsCredentialsProvider credentialsProvider,
                                                    final Region region) {
        return ApiGatewayClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public MemoryDbClient provideMemoryDbClient(final AwsCredentialsProvider credentialsProvider,
                                                final Region region) {
        return MemoryDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public Region provideRegion(final RunMetadata runMetadata) {
        return runMetadata.getRegion();
    }
}
