package com.cloudSavvy.aws.appstream;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.appstream.model.Fleet;
import software.amazon.awssdk.services.appstream.model.FleetType;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class AppStreamUsageRule implements AnalyzerRule {

    private AppStreamAccessor appStreamAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.AppStream_Fleet;

    @Override
    public AWSService getAWSService() {
        return AWSService.AppStream_2;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Fleet> fleets = appStreamAccessor.listFleets();

        if (CollectionUtils.isNullOrEmpty(fleets)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, fleets.stream()
                .map(fleet -> new ResourceMetadata(fleet.name(), fleet.createdTime())).collect(Collectors.toList())));

        List<Fleet> fleetsWithInstance =
                fleets.stream().filter(fleet -> fleet.fleetType() != FleetType.ELASTIC)
                        .filter(fleet -> TimeUtils.getElapsedTimeInDays(fleet.createdTime()) > ResourceAge.SEVEN_DAYS)
                        .collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(fleetsWithInstance)) {
            return ruleResult;
        }

        Map<String, Fleet> fleetNameMap =
                fleetsWithInstance.stream().collect(Collectors.toMap(Fleet::name, Function.identity()));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getAppStreamCapUtilizationMetricData(new ArrayList<>(fleetNameMap.keySet()));

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxCpuUtilization = MetricUtils.getMax(entry.getValue());
            if (maxCpuUtilization == 0) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.APP_STREAM_FLEET_NOT_USED));
            } else if (maxCpuUtilization < 20 && fleetNameMap.get(entry.getKey()).fleetType() == FleetType.ALWAYS_ON) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.APP_STREAM_ALWAYS_ON_FLEET_LOW_USAGE));
            }
        }

        return ruleResult;
    }
}
