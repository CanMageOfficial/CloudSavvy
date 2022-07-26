package com.cloudSavvy.aws.apigateway;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class ApiGatewayV2AuthRule implements AnalyzerRule {

    @Override
    public AWSService getAWSService() {
        return AWSService.API_GATEWAY;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {

        return new RuleResult();
    }
}
