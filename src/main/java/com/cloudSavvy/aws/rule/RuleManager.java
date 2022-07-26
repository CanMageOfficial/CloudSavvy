package com.cloudSavvy.aws.rule;

import com.cloudSavvy.common.run.RunMetadata;

public interface RuleManager {
    RuleContext setup(RunMetadata runMetadata);
}
