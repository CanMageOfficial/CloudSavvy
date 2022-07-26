package com.cloudSavvy.aws.s3;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class S3BucketRetentionRule implements AnalyzerRule {

    private final EntityType entityType = EntityType.S3_BUCKET;
    private S3Accessor s3Accessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.S3;
    }

    @Override
    public RuleResult call(final RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> localBuckets =  ruleContext.getLocalBuckets();

        if (CollectionUtils.isNullOrEmpty(localBuckets)) {
            return ruleResult;
        }

        List<String> assetBuckets = Collections.synchronizedList(new ArrayList<>());
        localBuckets.parallelStream().forEach(bucketName -> {

            if (bucketName.startsWith("lambda-artifacts")) {
                assetBuckets.add(bucketName);
            } else {
                List<Tag> tags = s3Accessor.listBucketTags(bucketName);
                if (!CollectionUtils.isNullOrEmpty(tags)) {
                    Optional<Tag> logicalIdTag = tags.stream()
                            .filter(tag -> "aws:cloudformation:logical-id".equals(tag.key())).findFirst();
                    if (logicalIdTag.isPresent()) {
                        String tagValue = logicalIdTag.get().value();
                        if ("DeploymentBucket".equals(tagValue) || "StagingBucket".equals(tagValue)) {
                            assetBuckets.add(bucketName);
                        }
                    }
                }
            }
        });

        if (CollectionUtils.isNullOrEmpty(assetBuckets)) {
            return ruleResult;
        }

        assetBuckets.parallelStream().forEach(bucketName -> {
            boolean hasLifecycleConfiguration = s3Accessor.hasLifecycleConfiguration(bucketName);
            if (!hasLifecycleConfiguration) {
                ruleResult.addIssueData(new IssueData(entityType, bucketName,
                        IssueCode.S3_DEVELOPMENT_BUCKET_HAS_NO_LIFECYCLE_CONFIG));
            }
        });

        return ruleResult;
    }
}
