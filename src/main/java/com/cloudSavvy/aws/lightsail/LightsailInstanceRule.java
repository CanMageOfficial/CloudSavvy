package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.Instance;
import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailInstanceRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_INSTANCE;

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Instance> instances = lightsailAccessor.listInstances();

        if (CollectionUtils.isNullOrEmpty(instances)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, instances.stream()
                .map(instance -> new ResourceMetadata(instance.name(), instance.createdAt()))
                .collect(Collectors.toList())));

        List<Instance> oldInstances = instances.stream()
                .filter(instance -> TimeUtils.getElapsedTimeInDays(instance.createdAt()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        oldInstances.stream().parallel().forEach(instance -> {
            List<MetricDatapoint> cpuMetricData = lightsailAccessor.getInstanceCPUUtilMetrics(instance.name());
            double max = LightSailUtils.getMaxOfMetricMax(cpuMetricData);
            if (max < 20) {
                ruleResult.addIssueData(new IssueData(entityType,
                        instance.name(), IssueCode.LIGHTSAIL_INSTANCE_HAS_LOW_USAGE));
            }
        });

        return ruleResult;
    }
}
