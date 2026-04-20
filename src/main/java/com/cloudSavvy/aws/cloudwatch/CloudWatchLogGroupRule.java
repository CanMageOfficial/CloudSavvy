package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.utils.CdkUtils;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.List;

@AllArgsConstructor
public class CloudWatchLogGroupRule implements AnalyzerRule {

    private CloudWatchLogAccessor logAccessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.CloudWatch;
    }

    private final EntityType entityType = EntityType.CLOUDWATCH_LOG_GROUP;

    private static final ImmutableSet<String> AWS_MANAGED_LOG_GROUPS = ImmutableSet.of(
            "/aws/apigateway/welcome"
    );

    private static final ImmutableSet<String> AWS_MANAGED_LOG_GROUP_PREFIXES = ImmutableSet.of(
            "API-Gateway-Execution-Logs_"
    );

    private boolean isAwsManaged(String logGroupName) {
        if (AWS_MANAGED_LOG_GROUPS.contains(logGroupName)) {
            return true;
        }
        for (String prefix : AWS_MANAGED_LOG_GROUP_PREFIXES) {
            if (logGroupName.startsWith(prefix)) {
                return true;
            }
        }
        // Lambda log groups are named /aws/lambda/<function-name>
        if (logGroupName.startsWith("/aws/lambda/")) {
            return CdkUtils.isCdkInternalLambda(logGroupName.substring("/aws/lambda/".length()));
        }
        return false;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<LogGroup> logGroups = logAccessor.getNoRetentionUsedLogGroups(10);
        logGroups.stream()
                .filter(logGroup -> !isAwsManaged(logGroup.logGroupName()))
                .forEach(logGroup -> ruleResult.addIssueData(new IssueData(entityType,
                        logGroup.logGroupName(), IssueCode.CLOUDWATCH_LOG_GROUP_HAS_NO_RETENTION)));

        return ruleResult;
    }
}
