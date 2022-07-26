package com.cloudSavvy.aws.rule;

import com.cloudSavvy.common.run.RunMetadata;

public class DefaultRuleManager implements RuleManager {
    public RuleContext setup(RunMetadata runMetadata) {
        return RuleContext.builder().build();
    }
}
