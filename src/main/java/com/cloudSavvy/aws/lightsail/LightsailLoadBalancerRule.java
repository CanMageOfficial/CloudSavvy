package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.InstanceHealthState;
import software.amazon.awssdk.services.lightsail.model.InstanceHealthSummary;
import software.amazon.awssdk.services.lightsail.model.LoadBalancer;
import software.amazon.awssdk.services.lightsail.model.LoadBalancerState;
import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailLoadBalancerRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_LOAD_BALANCER;

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<LoadBalancer> loadBalancers = lightsailAccessor.listLoadBalancers();

        if (CollectionUtils.isNullOrEmpty(loadBalancers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, loadBalancers.stream()
                .map(lb -> new ResourceMetadata(lb.name(), lb.createdAt()))
                .collect(Collectors.toList())));

        List<LoadBalancer> oldLoadBalancers = loadBalancers.stream()
                .filter(lb -> TimeUtils.getElapsedTimeInDays(lb.createdAt()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        List<LoadBalancer> oldWorkingLBs = getActiveLoadBalancers(oldLoadBalancers, ruleResult);

        if (CollectionUtils.isNullOrEmpty(oldWorkingLBs)) {
            return ruleResult;
        }

        List<LoadBalancer> lbsWithNoIssue = Collections.synchronizedList(new ArrayList<>());
        oldWorkingLBs.stream().parallel().forEach(lb -> {
            List<MetricDatapoint> cpuMetricData = lightsailAccessor.getLoadBalancerRequestCountMetrics(lb.name());
            double max = LightSailUtils.getMaxOfMetricSum(cpuMetricData);
            if (max < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        lb.name(), IssueCode.LIGHTSAIL_LOAD_BALANCER_IS_NOT_USED));
            } else {
                lbsWithNoIssue.add(lb);
            }
        });

        return ruleResult;
    }

    private List<LoadBalancer> getActiveLoadBalancers(List<LoadBalancer> loadBalancers, RuleResult ruleResult) {
        List<LoadBalancer> workingLBs = new ArrayList<>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            if (loadBalancer.state() == LoadBalancerState.ACTIVE_IMPAIRED) {
                ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                        IssueCode.LIGHTSAIL_LOAD_BALANCER_IS_ACTIVE_IMPAIRED));
            } else if (loadBalancer.state() == LoadBalancerState.FAILED) {
                ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                        IssueCode.LIGHTSAIL_LOAD_BALANCER_IS_FAILED));
            } else if (CollectionUtils.isNullOrEmpty(loadBalancer.instanceHealthSummary())) {
                ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                        IssueCode.LIGHTSAIL_LOAD_BALANCER_HAS_NO_TARGET));
            } else if (loadBalancer.instanceHealthSummary().size() == 1) {
                ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                        IssueCode.LIGHTSAIL_LOAD_BALANCER_HAS_ONLY_ONE_TARGET));
            } else {
                long unhealthyTargetCount =
                        getTargetCountByHealthState(loadBalancer, InstanceHealthState.UNHEALTHY);
                long unusedTargetCount =
                        getTargetCountByHealthState(loadBalancer, InstanceHealthState.UNUSED);
                long totalUnusableTarget = unhealthyTargetCount + unusedTargetCount;
                if (totalUnusableTarget > 0) {
                    if (totalUnusableTarget == loadBalancer.instanceHealthSummary().size()) {
                        ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                                IssueCode.LIGHTSAIL_LOAD_BALANCER_HAS_NO_HEALTHY_TARGET));
                    } else if (unhealthyTargetCount > 0) {
                        ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                                IssueCode.LIGHTSAIL_LOAD_BALANCER_HAS_UNHEALTHY_TARGET));
                    } else {
                        ruleResult.addIssueData(new IssueData(entityType, loadBalancer.name(),
                                IssueCode.LIGHTSAIL_LOAD_BALANCER_HAS_UNUSED_TARGET));
                    }

                } else {
                    workingLBs.add(loadBalancer);
                }
            }
        }
        return workingLBs;
    }

    private long getTargetCountByHealthState(LoadBalancer loadBalancer, InstanceHealthState stateToSearch) {
        List<InstanceHealthSummary> healthSummaries = loadBalancer.instanceHealthSummary();
        return healthSummaries.stream().filter(summary -> summary.instanceHealth() == stateToSearch).count();
    }
}
