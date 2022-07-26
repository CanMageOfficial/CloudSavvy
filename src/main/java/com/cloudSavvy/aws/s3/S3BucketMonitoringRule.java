package com.cloudSavvy.aws.s3;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.s3.model.LoggingEnabled;
import software.amazon.awssdk.services.s3.model.MetricsConfiguration;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class S3BucketMonitoringRule implements AnalyzerRule {
    private S3Accessor s3Accessor;

    private final EntityType entityType = EntityType.S3_BUCKET;

    @Override
    public AWSService getAWSService() {
        return AWSService.S3;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> localBuckets =  ruleContext.getLocalBuckets();

        if (CollectionUtils.isNullOrEmpty(localBuckets)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, localBuckets.stream()
                .map(bucket -> new ResourceMetadata(bucket, null))
                .collect(Collectors.toList())));

        localBuckets.parallelStream().forEach(bucketName -> {
            boolean isPublic = s3Accessor.isBucketPublic(bucketName);
            if (isPublic) {
                List<MetricsConfiguration> metricConfigList = s3Accessor.listBucketMetricConfigs(bucketName);
                if (CollectionUtils.isNullOrEmpty(metricConfigList)) {
                    LoggingEnabled loggingEnabled = s3Accessor.getBucketLoggingEnabled(bucketName);

                    if (loggingEnabled == null) {
                        ruleResult.addIssueData(new IssueData(entityType, bucketName,
                                IssueCode.S3_PUBLIC_BUCKET_HAS_NO_MONITORING));
                    }
                }
            }
        });

        return ruleResult;
    }
}
