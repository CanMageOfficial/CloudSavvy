package com.cloudSavvy.aws.cloudformation;

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
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CloudFormationStackRule implements AnalyzerRule {

    private CloudFormationAccessor cloudFormationAccessor;

    private final EntityType entityType = EntityType.CLOUDFORMATION_STACK;

    @Override
    public AWSService getAWSService() {
        return AWSService.CloudFormation;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Stack> failedStacks = cloudFormationAccessor.listFailedStacks();
        if (CollectionUtils.isNullOrEmpty(failedStacks)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, failedStacks.stream()
                .map(s -> new ResourceMetadata(s.stackName(), s.creationTime()))
                .collect(Collectors.toList())));

        for (Stack stack : failedStacks) {
            IssueCode issueCode = stack.stackStatus() == StackStatus.ROLLBACK_COMPLETE
                    ? IssueCode.CLOUDFORMATION_STACK_IN_ROLLBACK_COMPLETE
                    : IssueCode.CLOUDFORMATION_STACK_IN_FAILED_STATE;
            ruleResult.addIssueData(new IssueData(entityType, stack.stackName(), issueCode));
        }

        return ruleResult;
    }
}
