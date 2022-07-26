package com.cloudSavvy.aws.ec2;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class InstanceRule implements AnalyzerRule {

    // https://docs.aws.amazon.com/AWSEC2/latest/APIReference/API_InstanceState.html
    private static final int STOPPED_STATE = 80;
    private static final int TERMINATED = 48;

    private EC2Accessor ec2Accessor;

    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.EC2_INSTANCE;
    private static final int MIN_CPU_UTILIZATION = 20;

    @Override
    public AWSService getAWSService() {
        return AWSService.EC2;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Reservation> reservations = ec2Accessor.listInstances();
        List<Instance> instances = reservations.stream().map(Reservation::instances)
                .flatMap(List::stream)
                .filter(instance -> instance.state().code() != TERMINATED).collect(Collectors.toList());
        List<String> instanceIds = instances.stream()
                .map(Instance::instanceId).collect(Collectors.toList());
        for (Instance instance : instances) {
            if (instance.state().code() == STOPPED_STATE) {
                ruleResult.addIssueData(new IssueData(entityType,
                        instance.instanceId(), IssueCode.EC2_INSTANCE_STOPPED));
            }
        }

        ruleResult.addServiceData(new ServiceData(entityType, instances.stream()
                .map(instance -> new ResourceMetadata(instance.instanceId(), instance.launchTime()))
                .collect(Collectors.toList())));

        if (CollectionUtils.isNullOrEmpty(instanceIds)) {
            return ruleResult;
        }

        Map<String, MetricDataResult> metricDataResultMap = cloudWatchAccessor.getEC2InstanceCPUUtilMetricData(instanceIds);

        log.debug("instanceIdToDataResultMap: {}", metricDataResultMap);
        for (Map.Entry<String, MetricDataResult> entry : metricDataResultMap.entrySet()) {
            MetricDataResult dataResult = entry.getValue();
            if (!CollectionUtils.isNullOrEmpty(dataResult.values()) && dataResult.values().size() > ResourceAge.SEVEN_DAYS
                    && MetricUtils.getMax(dataResult) < MIN_CPU_UTILIZATION) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.EC2_INSTANCE_CPU_UTILIZATION_LOW));
            }
        }
        return ruleResult;
    }
}
