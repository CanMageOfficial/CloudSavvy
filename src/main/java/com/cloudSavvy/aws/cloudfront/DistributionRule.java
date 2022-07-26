package com.cloudSavvy.aws.cloudfront;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.SSLSupportMethod;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class DistributionRule implements AnalyzerRule {

    private CloudFrontAccessor cloudFrontAccessor;
    private CloudWatchAccessor cloudWatchAccessor;
    private final EntityType entityType = EntityType.CLOUDFRONT_DISTRIBUTION;

    @Override
    public AWSService getAWSService() {
        return AWSService.CloudFront;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DistributionSummary> distributions = cloudFrontAccessor.listDistributions();

        if (CollectionUtils.isNullOrEmpty(distributions)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, distributions.stream()
                .map(dist -> new ResourceMetadata(dist.id(), dist.lastModifiedTime()))
                .collect(Collectors.toList())));

        List<String> customCertDistributions = distributions.stream()
                .filter(dist -> !dist.viewerCertificate().cloudFrontDefaultCertificate()
                        && (dist.viewerCertificate().sslSupportMethod() == SSLSupportMethod.VIP
                        || dist.viewerCertificate().sslSupportMethod() == SSLSupportMethod.STATIC_IP))
                .map(DistributionSummary::id).collect(Collectors.toList());

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getCloudfrontDistRequestsMetricData(customCertDistributions);

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            MetricDataResult dataResult = entry.getValue();
            if (dataResult.values().size() > ResourceAge.SEVEN_DAYS && MetricUtils.getMax(dataResult) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.CLOUDFRONT_DISTRIBUTION_WITH_CUSTOM_SSL));
            }
        }

        return ruleResult;
    }
}