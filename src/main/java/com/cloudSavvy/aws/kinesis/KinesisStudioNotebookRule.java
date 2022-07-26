package com.cloudSavvy.aws.kinesis;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.metric.MetricConstants;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationStatus;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationSummary;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class KinesisStudioNotebookRule implements AnalyzerRule {

    private KinesisAccessor kinesisAccessor;
    private CloudWatchAccessor cloudWatchAccessor;
    private final EntityType entityType = EntityType.KINESIS_STUDIO_NOTEBOOK;

    @Override
    public AWSService getAWSService() {
        return AWSService.Kinesis;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<ApplicationSummary> applicationSummaries = kinesisAccessor.listAnalyticApplicationsV2();
        if (CollectionUtils.isNullOrEmpty(applicationSummaries)) {
            return ruleResult;
        }

        List<ApplicationDetail> applicationDetails = Collections.synchronizedList(new ArrayList<>());
        applicationSummaries.parallelStream().forEach(appSummary -> {
            ApplicationDetail appDetail = kinesisAccessor.describeAnalyticApplicationV2(appSummary.applicationName());
            applicationDetails.add(appDetail);
        });

        if (CollectionUtils.isNullOrEmpty(applicationDetails)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, applicationSummaries.stream()
                .map(appSummary -> new ResourceMetadata(appSummary.applicationName(), null))
                .collect(Collectors.toList())));

        List<ApplicationDetail> oldRunningApplications = applicationDetails.stream()
                .filter(app -> app.applicationStatus() == ApplicationStatus.RUNNING
                        && TimeUtils.getElapsedTimeInDays(app.createTimestamp()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldRunningApplications)) {
            return ruleResult;
        }
        List<String> oldRunningApplicationNames =
                oldRunningApplications.stream().map(ApplicationDetail::applicationName).collect(Collectors.toList());

        analyzeServerUsage(oldRunningApplicationNames, ruleResult);
        return ruleResult;
    }

    private void analyzeServerUsage(final List<String> applications, RuleResult ruleResult) {
        Map<String, MetricDataResult> metricsMap =
                cloudWatchAccessor.getKinesisZeppelinServerUptimeMetricData(applications);
        for (Map.Entry<String, MetricDataResult> entry : metricsMap.entrySet()) {
            if (isRunningLong(entry.getValue().values())) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.KINESIS_STUDIO_NOTEBOOK_RUNNING_LONG));
            }
        }
    }

    public boolean isRunningLong(List<Double> metricValues) {
        if (CollectionUtils.isNullOrEmpty(metricValues) || metricValues.size() < 8) {
            return false;
        }

        for (int i = metricValues.size() - 2; i > metricValues.size() - 8; i--) {
            if (metricValues.get(i) < MetricConstants.MILLISECONDS_IN_20_HOURS) {
                return false;
            }
        }

        return true;
    }
}