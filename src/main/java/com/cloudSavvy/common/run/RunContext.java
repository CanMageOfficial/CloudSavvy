package com.cloudSavvy.common.run;

import com.cloudSavvy.aws.rule.RuleManagerFactory;
import com.cloudSavvy.cache.GlobalCache;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RunContext {
    private RunMetadata runMetadata;
    private RunStatistics runStatistics;
    private GlobalCache globalCache;
    private RuleManagerFactory ruleManagerFactory;
}
