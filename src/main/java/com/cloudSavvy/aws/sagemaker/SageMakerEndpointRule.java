package com.cloudSavvy.aws.sagemaker;

import com.cloudSavvy.aws.cloudwatch.CloudWatchLogAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.sagemaker.model.EndpointSummary;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class SageMakerEndpointRule implements AnalyzerRule {

    private SageMakerAccessor sageMakerAccessor;
    private CloudWatchLogAccessor cloudWatchLogAccessor;

    private final EntityType entityType = EntityType.SAGEMAKER_ENDPOINT;
    private final String endpointsLogGroupPrefix = "/aws/sagemaker/Endpoints/";

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_SageMaker;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<EndpointSummary> endpointSummaries = sageMakerAccessor.listEndpoints();
        if (CollectionUtils.isNullOrEmpty(endpointSummaries)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, endpointSummaries.stream()
                .map(endpoint -> new ResourceMetadata(endpoint.endpointName(), endpoint.creationTime()))
                .collect(Collectors.toList())));

        List<EndpointSummary> oldEndpoints = endpointSummaries.stream()
                .filter(endpoint -> TimeUtils.getElapsedTimeInDays(endpoint.creationTime()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldEndpoints)) {
            return ruleResult;
        }

        List<LogGroup> logGroups = cloudWatchLogAccessor.getLogGroups(endpointsLogGroupPrefix);
        Map<String, LogGroup> logGroupNameMap =
                logGroups.stream().collect(Collectors.toMap(LogGroup::logGroupName, Function.identity()));

        List<EndpointSummary> oldRunningEndpoints = new ArrayList<>();
        for (EndpointSummary endpointSummary : oldEndpoints) {
            if (!logGroupNameMap.containsKey(endpointsLogGroupPrefix + endpointSummary.endpointName())) {
                ruleResult.addIssueData(new IssueData(entityType, endpointSummary.endpointName(),
                        IssueCode.SAGEMAKER_ENDPOINT_NOT_USED));
            } else {
                oldRunningEndpoints.add(endpointSummary);
            }
        }

        oldRunningEndpoints.parallelStream().forEach(endpoint -> {
            boolean isUnused = false;
            Optional<LogStream> logStream = cloudWatchLogAccessor
                    .getLogStreamByEventTime(endpointsLogGroupPrefix + endpoint.endpointName());
            if (logStream.isPresent()) {
                long lastEventTime = logStream.get().lastEventTimestamp();
                if (TimeUtils.getElapsedTimeInDays(lastEventTime) > ResourceAge.SEVEN_DAYS) {
                    isUnused = true;
                }
            } else {
                isUnused = true;
            }
            if (isUnused) {
                ruleResult.addIssueData(new IssueData(entityType, endpoint.endpointName(),
                        IssueCode.SAGEMAKER_ENDPOINT_NOT_USED));
            }
        });

        return ruleResult;
    }
}
