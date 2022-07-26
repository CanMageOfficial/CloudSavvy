package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class ApplicationLoadBalancerRule implements AnalyzerRule {

    private CloudWatchAccessor cloudWatchAccessor;
    private final EntityType entityType = EntityType.LOAD_BALANCER;

    @Override
    public AWSService getAWSService() {
        return AWSService.LOAD_BALANCER;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<LoadBalancer> loadBalancers = ruleContext.getV2LoadBalancers();
        if (CollectionUtils.isNullOrEmpty(loadBalancers)) {
            return ruleResult;
        }

        List<LoadBalancer> appLoadBalancers =
                LoadBalancerUtils.filterLoadBalancers(loadBalancers, LoadBalancerTypeEnum.APPLICATION);
        if (CollectionUtils.isNullOrEmpty(appLoadBalancers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, appLoadBalancers.stream()
                .map(lb -> new ResourceMetadata(lb.loadBalancerName(), lb.createdTime()))
                .collect(Collectors.toList())));

        List<LoadBalancer> oldLoadBalancers = LoadBalancerUtils.getOldLoadBalancers(loadBalancers);
        if (CollectionUtils.isNullOrEmpty(oldLoadBalancers)) {
            return ruleResult;
        }

        for (LoadBalancer lb : oldLoadBalancers) {
            if (lb.state().code() == LoadBalancerStateEnum.ACTIVE_IMPAIRED) {
                ruleResult.addIssueData(new IssueData(entityType, lb.loadBalancerName(),
                        IssueCode.LOAD_BALANCER_STATE_IS_ACTIVE_IMPAIRED));
            } else if (lb.state().code() == LoadBalancerStateEnum.FAILED) {
                ruleResult.addIssueData(new IssueData(entityType, lb.loadBalancerName(),
                        IssueCode.LOAD_BALANCER_STATE_IS_FAILED));
            }
        }

        List<LoadBalancer> oldWorkingLoadBalancers = LoadBalancerUtils.getWorkingLoadBalancers(oldLoadBalancers);
        analyzeApplicationLbs(ruleResult, oldWorkingLoadBalancers);

        return ruleResult;
    }

    private void analyzeApplicationLbs(RuleResult ruleResult, List<LoadBalancer> loadBalancers) {
        Map<String, LoadBalancer> metricNameMap =
                LoadBalancerUtils.getMetricNameMap(loadBalancers, LoadBalancerTypeEnum.APPLICATION);
        Map<String, MetricDataResult> metricResult =
                cloudWatchAccessor.getAppLBRequestCountMetricData(new ArrayList<>(metricNameMap.keySet()));

        for (Map.Entry<String, MetricDataResult> entry : metricResult.entrySet()) {
            if (MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        metricNameMap.get(entry.getKey()).loadBalancerName(),
                        IssueCode.APPLICATION_LOAD_BALANCER_NOT_USED));
            }
        }
    }
}
