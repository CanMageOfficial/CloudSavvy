package com.cloudSavvy.common;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;

public interface AnalyzerRule {
    AWSService getAWSService();

    RuleResult call(RuleContext context);
}
