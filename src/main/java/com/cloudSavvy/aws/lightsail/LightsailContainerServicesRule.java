package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.ContainerService;
import software.amazon.awssdk.services.lightsail.model.ContainerServiceState;
import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailContainerServicesRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_CONTAINER;

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<ContainerService> containers = lightsailAccessor.listContainerServices();

        if (CollectionUtils.isNullOrEmpty(containers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, containers.stream()
                .map(container -> new ResourceMetadata(container.containerServiceName(), container.createdAt()))
                .collect(Collectors.toList())));

        List<ContainerService> oldContainers = containers.stream()
                .filter(container -> TimeUtils.getElapsedTimeInDays(container.createdAt()) > ResourceAge.SEVEN_DAYS
                        && container.state() != ContainerServiceState.DELETING)
                .collect(Collectors.toList());

        List<ContainerService> oldWorkingContainers = new ArrayList<>();
        for (ContainerService container : oldContainers) {
            if (container.state() == ContainerServiceState.DISABLED) {
                ruleResult.addIssueData(new IssueData(entityType,
                        container.containerServiceName(), IssueCode.LIGHTSAIL_CONTAINER_IS_DISABLED));
            } else if (container.currentDeployment() == null && container.nextDeployment() == null) {
                ruleResult.addIssueData(new IssueData(entityType,
                        container.containerServiceName(), IssueCode.LIGHTSAIL_CONTAINER_IS_NOT_DEPLOYED));
            } else {
                oldWorkingContainers.add(container);
            }
        }

        if (CollectionUtils.isNullOrEmpty(oldWorkingContainers)) {
            return ruleResult;
        }

        oldWorkingContainers.stream().parallel().forEach(container -> {
            List<MetricDatapoint> cpuMetricData =
                    lightsailAccessor.getContainerCPUUtilMetrics(container.containerServiceName());
            double max = LightSailUtils.getMaxOfMetricMax(cpuMetricData);
            if (max < 20) {
                ruleResult.addIssueData(new IssueData(entityType,
                        container.containerServiceName(), IssueCode.LIGHTSAIL_CONTAINER_HAS_LOW_USAGE));
            }
        });

        return ruleResult;
    }
}
