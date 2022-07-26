package com.cloudSavvy.execution;

import com.cloudSavvy.aws.common.ServiceAvailability;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.ErrorData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.List;

@Slf4j
public class RuleExecutor {
    public static RegionAnalyzeResult execute(final @NonNull List<AnalyzerRule> rules,
                                              final @NonNull AWSService awsService,
                                              final @NonNull RunMetadata runMetadata,
                                              final @NonNull RuleManager ruleManager) {
        if (CollectionUtils.isNullOrEmpty(rules)) {
            throw new IllegalArgumentException("Rules are empty for service");
        }

        RegionAnalyzeResult regionResult = new RegionAnalyzeResult(runMetadata.getRegion());
        boolean isServiceAvailable = ServiceAvailability.isAvailable(awsService, runMetadata.getRegion());
        if (!ServiceAvailability.IGNORE_AVAILABILITY && !isServiceAvailable) {
            return regionResult;
        }

        long serviceRuleCount =
                rules.stream().filter(rule -> rule.getAWSService() == awsService)
                        .count();
        if (serviceRuleCount != rules.size() || serviceRuleCount == 0) {
            throw new IllegalArgumentException("Rules are not provided for service:" + awsService);
        }

        Instant startTime = Instant.now();
        try {
            RuleContext ruleContext = ruleManager.setup(runMetadata);
            rules.parallelStream().forEach(rule -> {
                RuleResult ruleResult = rule.call(ruleContext);
                regionResult.merge(ruleResult);
            });

            if (!isServiceAvailable && !ServiceAvailability.AVAILABLE_REGIONS.containsKey(awsService)) {
                // This code is only reached during development environment
                throw new RuntimeException("Service availability is marked wrong.");
            }
        } catch (Exception e) {
            log.error("Connection to {} service failed in region {}.", awsService, runMetadata.getRegion(), e);
            regionResult.addErrorData(new ErrorData(awsService, e.getMessage()));
        }

        Instant finishTime = Instant.now();
        regionResult.addServiceExecutionTime(awsService, TimeUtils.calcDiffInMillis(startTime, finishTime));

        return regionResult;
    }
}
