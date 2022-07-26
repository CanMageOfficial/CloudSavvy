package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
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

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<LogGroup> logGroups = logAccessor.getNoRetentionUsedLogGroups(10);
        logGroups.forEach(logGroup -> ruleResult.addIssueData(new IssueData(entityType,
                logGroup.logGroupName(), IssueCode.CLOUDWATCH_LOG_GROUP_HAS_NO_RETENTION)));

        return ruleResult;
    }
}
