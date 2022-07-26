package com.cloudSavvy.execution;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.rule.RuleManager;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.run.RunContext;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.common.run.RunStatistics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class AWSAnalyzer {
    private List<AnalyzerRule> rules;
    private RunContext runContext;

    private AWSService awsService;

    public RegionAnalyzeResult call(final RunMetadata runMetadata) {
        validateRules();
        RunStatistics runStatistics = runContext.getRunStatistics();
        runStatistics.startRun(runMetadata.getRegion(), awsService);
        log.debug("Analyzing {} component in {} region", awsService, runMetadata.getRegion());

        RuleManager ruleManager = runContext.getRuleManagerFactory().getRuleManager(awsService);
        RegionAnalyzeResult regionAnalyzeResult =
                RuleExecutor.execute(rules, awsService, runMetadata, ruleManager);

        boolean isSucceeded = CollectionUtils.isNullOrEmpty(regionAnalyzeResult.getErrorDataList());
        runStatistics.finishRun(runMetadata.getRegion(), awsService, isSucceeded);
        return regionAnalyzeResult;
    }

    private void validateRules() {
        HashSet<Class<? extends AnalyzerRule>> set = new HashSet<>();
        rules.forEach(rule -> {
                Class<? extends AnalyzerRule> classItem = rule.getClass();
                if (set.contains(classItem)) {
                    throw new IllegalArgumentException("Rules have duplicate rule:" + classItem);
                }
                set.add(classItem);
        });
    }
}
