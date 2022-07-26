package com.cloudSavvy.dagger;

import com.cloudSavvy.aws.appstream.AppStreamAccessor;
import com.cloudSavvy.aws.appstream.AppStreamUsageRule;
import com.cloudSavvy.aws.autoscaling.AutoScalingAccessor;
import com.cloudSavvy.aws.cloudfront.CloudFrontAccessor;
import com.cloudSavvy.aws.cloudfront.DistributionRule;
import com.cloudSavvy.aws.cloudsearch.CloudSearchAccessor;
import com.cloudSavvy.aws.cloudsearch.CloudSearchDomainUsageRule;
import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.cloudwatch.CloudWatchBillingRule;
import com.cloudSavvy.aws.cloudwatch.CloudWatchLogAccessor;
import com.cloudSavvy.aws.cloudwatch.CloudWatchLogGroupRule;
import com.cloudSavvy.aws.dynamodb.DynamoDbAccessor;
import com.cloudSavvy.aws.dynamodb.DynamoDbRuleManager;
import com.cloudSavvy.aws.dynamodb.DynamoDbTableUsageRule;
import com.cloudSavvy.aws.ec2.EC2Accessor;
import com.cloudSavvy.aws.ec2.ElasticIpAddressRule;
import com.cloudSavvy.aws.ec2.InstanceRule;
import com.cloudSavvy.aws.ec2.NatGatewayRule;
import com.cloudSavvy.aws.ec2.VolumeRule;
import com.cloudSavvy.aws.ecs.ECSAccessor;
import com.cloudSavvy.aws.efs.EfsRuleManager;
import com.cloudSavvy.aws.eks.EKSAccessor;
import com.cloudSavvy.aws.eks.EKSClusterUsageRule;
import com.cloudSavvy.aws.elasticache.ElastiCacheAccessor;
import com.cloudSavvy.aws.elasticache.MemCachedClusterRule;
import com.cloudSavvy.aws.elasticache.RedisClusterRule;
import com.cloudSavvy.aws.eventbridge.EventBridgeAccessor;
import com.cloudSavvy.aws.eventbridge.EventBridgeArchiveRule;
import com.cloudSavvy.aws.fsx.FSxAccessor;
import com.cloudSavvy.aws.fsx.FSxUsageRule;
import com.cloudSavvy.aws.glue.GlueAccessor;
import com.cloudSavvy.aws.glue.GlueDevEndPointRule;
import com.cloudSavvy.aws.kinesis.KinesisAccessor;
import com.cloudSavvy.aws.kinesis.KinesisDataStreamRule;
import com.cloudSavvy.aws.kinesis.KinesisStudioNotebookRule;
import com.cloudSavvy.aws.lambda.LambdaAccessor;
import com.cloudSavvy.aws.lambda.LambdaFunctionUsageRule;
import com.cloudSavvy.aws.lightsail.LightsailContainerServicesRule;
import com.cloudSavvy.aws.lightsail.LightsailRelationalDBRule;
import com.cloudSavvy.aws.loadbalancer.ApplicationLoadBalancerRule;
import com.cloudSavvy.aws.loadbalancer.ClassicLoadBalancerRule;
import com.cloudSavvy.aws.loadbalancer.LoadBalancerAccessor;
import com.cloudSavvy.aws.loadbalancer.LoadBalancerRuleManager;
import com.cloudSavvy.aws.loadbalancer.NetworkLoadBalancerRule;
import com.cloudSavvy.aws.loadbalancer.TargetGroupRule;
import com.cloudSavvy.aws.rds.DBClusterRule;
import com.cloudSavvy.aws.rds.DBInstanceRule;
import com.cloudSavvy.aws.rds.DBProxyRule;
import com.cloudSavvy.aws.rds.RDSAccessor;
import com.cloudSavvy.aws.redshift.RedshiftAccessor;
import com.cloudSavvy.aws.redshift.RedshiftClusterRule;
import com.cloudSavvy.aws.redshift.RedshiftClusterUsageRule;
import com.cloudSavvy.aws.redshift.ServerlessWorkgroupRule;
import com.cloudSavvy.aws.rule.DefaultRuleManager;
import com.cloudSavvy.aws.rule.RuleManager;
import com.cloudSavvy.aws.rule.RuleManagerFactory;
import com.cloudSavvy.aws.s3.S3Accessor;
import com.cloudSavvy.aws.s3.S3BucketMonitoringRule;
import com.cloudSavvy.aws.s3.S3BucketRetentionRule;
import com.cloudSavvy.aws.s3.S3BucketUsageRule;
import com.cloudSavvy.aws.s3.S3RuleManager;
import com.cloudSavvy.aws.sagemaker.SageMakerAccessor;
import com.cloudSavvy.aws.sagemaker.SageMakerEndpointRule;
import com.cloudSavvy.aws.sagemaker.SageMakerInstanceRule;
import com.cloudSavvy.aws.transfer.AWSTransferAccessor;
import com.cloudSavvy.aws.transfer.AWSTransferServerRule;
import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.run.RunContext;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.common.run.RunStatistics;
import com.cloudSavvy.execution.AWSAnalyzer;
import com.cloudSavvy.aws.apigateway.ApiGatewayAccessor;
import com.cloudSavvy.aws.apigateway.ApiGatewayAuthRule;
import com.cloudSavvy.aws.apigateway.ApiGatewayV2AuthRule;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.ecs.ECSServiceRule;
import com.cloudSavvy.aws.efs.EFSAccessor;
import com.cloudSavvy.aws.efs.EFSFileSystemLifecycleRule;
import com.cloudSavvy.aws.efs.EFSFileSystemUsageRule;
import com.cloudSavvy.aws.lightsail.LightsailAccessor;
import com.cloudSavvy.aws.lightsail.LightsailDiskRule;
import com.cloudSavvy.aws.lightsail.LightsailInstanceRule;
import com.cloudSavvy.aws.lightsail.LightsailLoadBalancerRule;
import com.cloudSavvy.aws.lightsail.LightsailStaticIPRule;
import com.cloudSavvy.aws.memorydb.MemoryDbAccessor;
import com.cloudSavvy.aws.memorydb.MemoryDbClusterRule;
import com.cloudSavvy.aws.redshift.RedshiftRuleManager;
import com.cloudSavvy.aws.secretsmanager.SecretsManagerAccessor;
import com.cloudSavvy.aws.secretsmanager.SecretsManagerUsageRule;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class AWSAnalyzerModule {

    private static final String VPC_RULES = "VPC_rules";
    private static final String EC2_RULES = "EC2_rules";
    private static final String LOAD_BALANCER_RULES = "LOAD_BALANCER_rules";
    private static final String KiNESiS_RULES = "Kinesis_rules";
    private static final String RDS_RULES = "RDS_rules";
    private static final String Redshift_RULES = "Redshift_RULES";
    private static final String CloudFront_RULES = "CloudFront_RULES";
    private static final String ElastiCache_RULES = "ElastiCache_RULES";
    private static final String EventBridge_RULES = "EventBridge_RULES";
    private static final String SageMaker_RULES = "SageMaker_RULES";
    private static final String EFS_RULES = "EFS_RULES";
    private static final String CLOUDWATCH_RULES = "CLOUDWATCH_RULES";
    private static final String DYNAMODB_RULES = "DYNAMODB_RULES";
    private static final String LAMBDA_RULES = "LAMBDA_RULES";
    private static final String S3_RULES = "S3_RULES";
    private static final String AWS_TRANSFER_RULES = "AWS_TRANSFER_RULES";
    private static final String FSX_RULES = "FSX_RULES";
    private static final String GLUE_RULES = "GLUE_RULES";
    private static final String AppStream_RULES = "AppStream_RULES";
    private static final String EKS_RULES = "EKS_RULES";
    private static final String Lightsail_RULES = "Lightsail_RULES";
    private static final String CloudSearch_RULES = "CloudSearch_RULES";
    private static final String ECS_RULES = "ECS_RULES";
    private static final String SECRETS_MANAGER_RULES = "SECRETS_MANAGER_RULES";
    private static final String API_GATEWAY_RULES = "API_GATEWAY_RULES";
    private static final String MEMORY_DB_RULES = "MEMORY_DB_RULES";

    @Provides
    @Singleton
    public RuleManagerFactory provideRuleManagerFactory(final Map<AWSService, RuleManager> ruleManagerMap) {
        DefaultRuleManager defaultRuleManager = new DefaultRuleManager();
        return new RuleManagerFactory(ruleManagerMap, defaultRuleManager);
    }

    @Provides
    @IntoMap
    @Singleton
    @AWSServiceKey(AWSService.DynamoDB)
    public RuleManager provideDynamoDbRuleManager(final DynamoDbAccessor dynamoDbAccessor) {
        return new DynamoDbRuleManager(dynamoDbAccessor);
    }

    @Provides
    @IntoMap
    @Singleton
    @AWSServiceKey(AWSService.EFS)
    public RuleManager provideEFSRuleManager(final EFSAccessor efsAccessor) {
        return new EfsRuleManager(efsAccessor);
    }

    @Provides
    @IntoMap
    @Singleton
    @AWSServiceKey(AWSService.Amazon_Redshift)
    public RuleManager provideRedshiftRuleManager(final RedshiftAccessor redshiftAccessor) {
        return new RedshiftRuleManager(redshiftAccessor);
    }

    @Provides
    @IntoMap
    @Singleton
    @AWSServiceKey(AWSService.LOAD_BALANCER)
    public RuleManager provideLoadBalancerRuleManager(final LoadBalancerAccessor loadBalancerAccessor) {
        return new LoadBalancerRuleManager(loadBalancerAccessor);
    }

    @Provides
    @IntoMap
    @Singleton
    @AWSServiceKey(AWSService.S3)
    public RuleManager provideS3RuleManager(final S3Accessor s3Accessor, final GlobalCache globalCache) {
        return new S3RuleManager(s3Accessor, globalCache);
    }

    @Provides
    @Singleton
    public RunContext provideRunContext(final RunMetadata runMetadata,
                                        final RunStatistics runStatistics,
                                        final GlobalCache globalCache,
                                        final RuleManagerFactory ruleManagerFactory) {
        return new RunContext(runMetadata, runStatistics, globalCache, ruleManagerFactory);
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideS3Analyzer(final @Named(S3_RULES) List<AnalyzerRule> rules,
                                         final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.S3);
    }

    @Provides
    @Singleton
    @Named(S3_RULES)
    public List<AnalyzerRule> provideS3AnalyzerRules(final S3Accessor s3Accessor,
                                                     final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new S3BucketUsageRule(cloudWatchAccessor));
        rules.add(new S3BucketMonitoringRule(s3Accessor));
        rules.add(new S3BucketRetentionRule(s3Accessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideDynamoDbAnalyzer(final @Named(DYNAMODB_RULES) List<AnalyzerRule> rules,
                                               final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.DynamoDB);
    }

    @Provides
    @Singleton
    @Named(DYNAMODB_RULES)
    public List<AnalyzerRule> provideDynamoDbAnalyzerRules(final DynamoDbAccessor dynamoDbAccessor,
                                                           final AutoScalingAccessor autoScalingAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new DynamoDbTableUsageRule(dynamoDbAccessor, autoScalingAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideLambdaAnalyzer(final @Named(LAMBDA_RULES) List<AnalyzerRule> rules,
                                             final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Lambda);
    }

    @Provides
    @Singleton
    @Named(LAMBDA_RULES)
    public List<AnalyzerRule> provideLambdaAnalyzerRules(final LambdaAccessor lambdaAccessor,
                                                         final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new LambdaFunctionUsageRule(lambdaAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @Named(KiNESiS_RULES)
    public List<AnalyzerRule> provideKinesisRules(final KinesisAccessor kinesisAccessor,
                                                  final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new KinesisDataStreamRule(kinesisAccessor, cloudWatchAccessor));
        rules.add(new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideKinesisAnalyzer(@Named(KiNESiS_RULES) List<AnalyzerRule> rules,
                                              RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Kinesis);
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideCloudWatchAnalyzer(final @Named(CLOUDWATCH_RULES) List<AnalyzerRule> rules,
                                                 final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.CloudWatch);
    }

    @Provides
    @Singleton
    @Named(CLOUDWATCH_RULES)
    public List<AnalyzerRule> provideCloudWatchAnalyzerRules(final CloudWatchLogAccessor logAccessor,
                                                             final CloudWatchAccessor cloudWatchAccessor,
                                                             final RunContext runContext) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new CloudWatchLogGroupRule(logAccessor));
        rules.add(new CloudWatchBillingRule(cloudWatchAccessor, runContext));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideRDSAnalyzer(final @Named(RDS_RULES) List<AnalyzerRule> rules,
                                          final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.RDS);
    }

    @Provides
    @Singleton
    @Named(RDS_RULES)
    public List<AnalyzerRule> provideRDSAnalyzerRules(final RDSAccessor rdsAccessor,
                                                      final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new DBClusterRule(rdsAccessor, cloudWatchAccessor));
        rules.add(new DBInstanceRule(rdsAccessor, cloudWatchAccessor));
        rules.add(new DBProxyRule(rdsAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideRedshiftAnalyzer(final @Named(Redshift_RULES) List<AnalyzerRule> rules,
                                               final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Amazon_Redshift);
    }

    @Provides
    @Singleton
    @Named(Redshift_RULES)
    public List<AnalyzerRule> provideRedshiftAnalyzerRules(final RedshiftAccessor redshiftAccessor,
                                                           final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new ServerlessWorkgroupRule(redshiftAccessor, cloudWatchAccessor));
        rules.add(new RedshiftClusterRule(cloudWatchAccessor));
        rules.add(new RedshiftClusterUsageRule(cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideCloudFrontAnalyzer(@Named(CloudFront_RULES) List<AnalyzerRule> rules,
                                                 RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.CloudFront);
    }

    @Provides
    @Singleton
    @Named(CloudFront_RULES)
    public List<AnalyzerRule> provideCloudFrontAnalyzerRules(CloudFrontAccessor cloudFrontAccessor,
                                                             CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new DistributionRule(cloudFrontAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideElastiCacheAnalyzer(@Named(ElastiCache_RULES) List<AnalyzerRule> rules,
                                                  RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.ElastiCache);
    }

    @Provides
    @Singleton
    @Named(ElastiCache_RULES)
    public List<AnalyzerRule> provideElastiCacheAnalyzerRules(ElastiCacheAccessor elastiCacheAccessor,
                                                              CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new RedisClusterRule(elastiCacheAccessor, cloudWatchAccessor));
        rules.add(new MemCachedClusterRule(elastiCacheAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideEFSAnalyzer(final @Named(EFS_RULES) List<AnalyzerRule> rules,
                                          final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.EFS);
    }

    @Provides
    @Singleton
    @Named(EFS_RULES)
    public List<AnalyzerRule> provideEFSAnalyzerRules(final EFSAccessor efsAccessor,
                                                      final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new EFSFileSystemUsageRule(efsAccessor, cloudWatchAccessor));
        rules.add(new EFSFileSystemLifecycleRule(efsAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideLightsailAnalyzer(final @Named(Lightsail_RULES) List<AnalyzerRule> rules,
                                                final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Lightsail);
    }

    @Provides
    @Singleton
    @Named(Lightsail_RULES)
    public List<AnalyzerRule> provideLightsailAnalyzerRules(final LightsailAccessor lightsailAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new LightsailInstanceRule(lightsailAccessor));
        rules.add(new LightsailContainerServicesRule(lightsailAccessor));
        rules.add(new LightsailRelationalDBRule(lightsailAccessor));
        rules.add(new LightsailStaticIPRule(lightsailAccessor));
        rules.add(new LightsailLoadBalancerRule(lightsailAccessor));
        rules.add(new LightsailDiskRule(lightsailAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideAWSTransferAnalyzer(final @Named(AWS_TRANSFER_RULES) List<AnalyzerRule> rules,
                                                  final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.AWS_Transfer);
    }

    @Provides
    @Singleton
    @Named(AWS_TRANSFER_RULES)
    public List<AnalyzerRule> provideAWSTransferAnalyzerRules(final AWSTransferAccessor transferAccessor,
                                                              final CloudWatchLogAccessor logAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new AWSTransferServerRule(transferAccessor, logAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideECSAnalyzer(final @Named(ECS_RULES) List<AnalyzerRule> rules,
                                          final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.ECS);
    }

    @Provides
    @Singleton
    @Named(ECS_RULES)
    public List<AnalyzerRule> provideECSAnalyzerRules(final ECSAccessor ecsAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new ECSServiceRule(ecsAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideFSxAnalyzer(@Named(FSX_RULES) List<AnalyzerRule> rules,
                                          RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.FSx);
    }

    @Provides
    @Singleton
    @Named(FSX_RULES)
    public List<AnalyzerRule> provideFSxAnalyzerRules(FSxAccessor fsxAccessor,
                                                      CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new FSxUsageRule(fsxAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideGlueAnalyzer(@Named(GLUE_RULES) List<AnalyzerRule> rules,
                                           RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.GLUE);
    }

    @Provides
    @Singleton
    @Named(GLUE_RULES)
    public List<AnalyzerRule> provideGlueAnalyzerRules(GlueAccessor glueAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new GlueDevEndPointRule(glueAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideAppStreamAnalyzer(@Named(AppStream_RULES) List<AnalyzerRule> rules,
                                                RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.AppStream_2);
    }

    @Provides
    @Singleton
    @Named(AppStream_RULES)
    public List<AnalyzerRule> provideAppStreamAnalyzerRules(AppStreamAccessor appStreamAccessor,
                                                            CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new AppStreamUsageRule(appStreamAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideEKSAnalyzer(@Named(EKS_RULES) List<AnalyzerRule> rules,
                                          RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.EKS);
    }

    @Provides
    @Singleton
    @Named(EKS_RULES)
    public List<AnalyzerRule> provideEKSAnalyzerRules(EKSAccessor eksAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new EKSClusterUsageRule(eksAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideEventBridgeAnalyzer(@Named(EventBridge_RULES) List<AnalyzerRule> rules,
                                                  RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Amazon_EventBridge);
    }

    @Provides
    @Singleton
    @Named(EventBridge_RULES)
    public List<AnalyzerRule> provideEventBridgeAnalyzerRules(EventBridgeAccessor eventBridgeAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new EventBridgeArchiveRule(eventBridgeAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideSageMakerAnalyzer(final @Named(SageMaker_RULES) List<AnalyzerRule> rules,
                                                final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.Amazon_SageMaker);
    }

    @Provides
    @Singleton
    @Named(SageMaker_RULES)
    public List<AnalyzerRule> provideSageMakerAnalyzerRules(final SageMakerAccessor sageMakerAccessor,
                                                            final CloudWatchLogAccessor logAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new SageMakerInstanceRule(sageMakerAccessor, logAccessor));
        rules.add(new SageMakerEndpointRule(sageMakerAccessor, logAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideCloudSearchAnalyzer(final @Named(CloudSearch_RULES) List<AnalyzerRule> rules,
                                                  final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.CloudSearch);
    }

    @Provides
    @Singleton
    @Named(CloudSearch_RULES)
    public List<AnalyzerRule> provideCloudSearchAnalyzerRules(final CloudSearchAccessor cloudSearchAccessor,
                                                              final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new CloudSearchDomainUsageRule(cloudSearchAccessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @Named(EC2_RULES)
    public List<AnalyzerRule> provideEC2AnalyzerRules(final EC2Accessor ec2Accessor,
                                                      final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new InstanceRule(ec2Accessor, cloudWatchAccessor));
        rules.add(new VolumeRule(ec2Accessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideEC2Analyzer(final @Named(EC2_RULES) List<AnalyzerRule> rules,
                                          final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.EC2);
    }

    @Provides
    @Singleton
    @Named(LOAD_BALANCER_RULES)
    public List<AnalyzerRule> provideLBAnalyzerRules(final CloudWatchAccessor cloudWatchAccessor,
                                                     final LoadBalancerAccessor loadBalancerAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new ApplicationLoadBalancerRule(cloudWatchAccessor));
        rules.add(new NetworkLoadBalancerRule(cloudWatchAccessor));
        rules.add(new ClassicLoadBalancerRule(loadBalancerAccessor));
        rules.add(new TargetGroupRule(loadBalancerAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideLoadBalancerAnalyzer(final @Named(LOAD_BALANCER_RULES) List<AnalyzerRule> rules,
                                                   final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.LOAD_BALANCER);
    }

    @Provides
    @Singleton
    @Named(VPC_RULES)
    public List<AnalyzerRule> provideVPCAnalyzerRules(final EC2Accessor ec2Accessor,
                                                      final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new ElasticIpAddressRule(ec2Accessor));
        rules.add(new NatGatewayRule(ec2Accessor, cloudWatchAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideVPCAnalyzer(final @Named(VPC_RULES) List<AnalyzerRule> rules,
                                          final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.VPC);
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideSecretsManagerAnalyzer(final @Named(SECRETS_MANAGER_RULES) List<AnalyzerRule> rules,
                                                     final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.SECRETS_MANAGER);
    }

    @Provides
    @Singleton
    @Named(SECRETS_MANAGER_RULES)
    public List<AnalyzerRule> provideSecretsManagerAnalyzerRules(final SecretsManagerAccessor secretsManagerAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new SecretsManagerUsageRule(secretsManagerAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideApiGatewayAnalyzer(final @Named(API_GATEWAY_RULES) List<AnalyzerRule> rules,
                                                 final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.API_GATEWAY);
    }

    @Provides
    @Singleton
    @Named(API_GATEWAY_RULES)
    public List<AnalyzerRule> provideApiGatewayAnalyzerRules(final ApiGatewayAccessor apiGatewayAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new ApiGatewayV2AuthRule());
        rules.add(new ApiGatewayAuthRule(apiGatewayAccessor));
        return rules;
    }

    @Provides
    @Singleton
    @IntoSet
    public AWSAnalyzer provideMemoryDbAnalyzer(final @Named(MEMORY_DB_RULES) List<AnalyzerRule> rules,
                                               final RunContext runContext) {
        return new AWSAnalyzer(rules, runContext, AWSService.MEMORY_DB);
    }

    @Provides
    @Singleton
    @Named(MEMORY_DB_RULES)
    public List<AnalyzerRule> provideMemoryDbAnalyzerRules(final MemoryDbAccessor memoryDbAccessor,
                                                           final CloudWatchAccessor cloudWatchAccessor) {
        List<AnalyzerRule> rules = new ArrayList<>();
        rules.add(new MemoryDbClusterRule(memoryDbAccessor, cloudWatchAccessor));
        return rules;
    }
}
