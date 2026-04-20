package com.cloudSavvy.aws.stepfunctions;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class StepFunctionsStateMachineRule implements AnalyzerRule {

    private StepFunctionsAccessor stepFunctionsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.STEP_FUNCTIONS_STATE_MACHINE;

    @Override
    public AWSService getAWSService() {
        return AWSService.StepFunctions;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<StateMachineListItem> stateMachines = stepFunctionsAccessor.listStateMachines();
        if (CollectionUtils.isNullOrEmpty(stateMachines)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, stateMachines.stream()
                .map(sm -> new ResourceMetadata(sm.name(), sm.creationDate()))
                .collect(Collectors.toList())));

        List<String> arns = stateMachines.stream()
                .map(StateMachineListItem::stateMachineArn)
                .collect(Collectors.toList());

        Map<String, StateMachineListItem> arnToStateMachine = stateMachines.stream()
                .collect(Collectors.toMap(StateMachineListItem::stateMachineArn, sm -> sm));

        Map<String, MetricDataResult> failureMetrics =
                cloudWatchAccessor.getStepFunctionsExecutionsFailedMetricData(arns);
        log.debug("Step Functions failure metrics: {}", failureMetrics);

        for (Map.Entry<String, MetricDataResult> entry : failureMetrics.entrySet()) {
            MetricDataResult dataResult = entry.getValue();
            if (!CollectionUtils.isNullOrEmpty(dataResult.values())) {
                double totalFailures = dataResult.values().stream().mapToDouble(Double::doubleValue).sum();
                if (totalFailures > 0) {
                    StateMachineListItem sm = arnToStateMachine.get(entry.getKey());
                    String name = sm != null ? sm.name() : entry.getKey();
                    ruleResult.addIssueData(new IssueData(entityType, name,
                            IssueCode.STEP_FUNCTIONS_STATE_MACHINE_HAS_FAILURES));
                }
            }
        }

        return ruleResult;
    }
}
