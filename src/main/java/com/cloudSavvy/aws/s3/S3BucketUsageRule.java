package com.cloudSavvy.aws.s3;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class S3BucketUsageRule implements AnalyzerRule {
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.S3_BUCKET;

    @Override
    public AWSService getAWSService() {
        return AWSService.S3;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> localBuckets = ruleContext.getLocalBuckets();

        if (CollectionUtils.isNullOrEmpty(localBuckets)) {
            return ruleResult;
        }

        Map<String, MetricDataResult> bucketSizeDataMap = cloudWatchAccessor.getS3BucketSizeBytesMetricData(localBuckets);

        for (Map.Entry<String, MetricDataResult> entry : bucketSizeDataMap.entrySet()) {
            MetricDataResult dataResult = entry.getValue();
            double sizeChangeInBytes = sizeChangeIn2Weeks(dataResult.values());
            double sizeChangeInGib = MetricUtils.convertToGiB(sizeChangeInBytes);
            if (sizeChangeInGib > 10) {
                double lastSize = MetricUtils.convertToGiB(dataResult.values().get(dataResult.values().size() - 1));
                if (lastSize > 100) {
                    ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                            IssueCode.S3_BUCKET_IS_GROWING_FAST));
                }
            }
        }

        return ruleResult;
    }

    public Double sizeChangeIn2Weeks(List<Double> values) {
        if (CollectionUtils.isNullOrEmpty(values) || values.size() < 14) {
            return 0.0;
        }

        return values.get(values.size() - 1) - values.get(values.size() - 14);
    }
}
