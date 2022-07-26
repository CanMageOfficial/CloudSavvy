package com.cloudSavvy.aws.rule;

import com.cloudSavvy.aws.common.AWSService;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class RuleManagerFactory {
    private Map<AWSService, RuleManager> ruleManagerMap;
    private DefaultRuleManager defaultRuleManager;

    public RuleManager getRuleManager(AWSService service) {
        return ruleManagerMap.getOrDefault(service, defaultRuleManager);
    }
}
