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
import software.amazon.awssdk.services.sagemaker.model.NotebookInstanceStatus;
import software.amazon.awssdk.services.sagemaker.model.NotebookInstanceSummary;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class SageMakerInstanceRule implements AnalyzerRule {

    private SageMakerAccessor sageMakerAccessor;
    private CloudWatchLogAccessor cloudWatchLogAccessor;

    private final EntityType entityType = EntityType.SAGEMAKER_NOTEBOOK_INSTANCE;
    private final String instanceLogGroup = "/aws/sagemaker/NotebookInstances";

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_SageMaker;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<NotebookInstanceSummary> instances = sageMakerAccessor.listNotebookInstances();
        if (CollectionUtils.isNullOrEmpty(instances)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, instances.stream()
                .map(instance -> new ResourceMetadata(instance.notebookInstanceName(), instance.creationTime()))
                .collect(Collectors.toList())));

        List<NotebookInstanceSummary> oldRunningInstances = instances.stream()
                .filter(instance -> TimeUtils.getElapsedTimeInDays(instance.creationTime()) > ResourceAge.SEVEN_DAYS
                        && instance.notebookInstanceStatus() != NotebookInstanceStatus.STOPPED
                        && instance.notebookInstanceStatus() != NotebookInstanceStatus.STOPPING)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldRunningInstances)) {
            return ruleResult;
        }

        Optional<LogGroup> logGroup = cloudWatchLogAccessor.getLogGroup(instanceLogGroup);
        if (logGroup.isEmpty()) {
            for (NotebookInstanceSummary instanceSummary : oldRunningInstances) {
                ruleResult.addIssueData(new IssueData(entityType, instanceSummary.notebookInstanceName(),
                        IssueCode.SAGEMAKER_NOTEBOOK_INSTANCE_NOT_USED));
            }
            return ruleResult;
        }

        oldRunningInstances.parallelStream().forEach(instance -> {
            boolean isUnused = false;
            String logStreamName = instance.notebookInstanceName() + "/jupyter.log";
            Optional<LogStream> logStream = cloudWatchLogAccessor.getLogStream(instanceLogGroup, logStreamName);
            if (logStream.isPresent()) {
                long lastEventTime = logStream.get().lastEventTimestamp();
                if (TimeUtils.getElapsedTimeInDays(lastEventTime) > ResourceAge.SEVEN_DAYS) {
                    isUnused = true;
                }
            } else {
                isUnused = true;
            }
            if (isUnused) {
                ruleResult.addIssueData(new IssueData(entityType, instance.notebookInstanceName(),
                        IssueCode.SAGEMAKER_NOTEBOOK_INSTANCE_NOT_USED));
            }
        });

        return ruleResult;
    }
}
