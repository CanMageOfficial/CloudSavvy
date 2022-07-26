package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class NetworkLoadBalancerRule implements AnalyzerRule {

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

        List<LoadBalancer> netLoadBalancers =
                LoadBalancerUtils.filterLoadBalancers(loadBalancers, LoadBalancerTypeEnum.NETWORK);
        if (CollectionUtils.isNullOrEmpty(netLoadBalancers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, netLoadBalancers.stream()
                .map(lb -> new ResourceMetadata(lb.loadBalancerName(), lb.createdTime()))
                .collect(Collectors.toList())));

        List<LoadBalancer> oldLoadBalancers = LoadBalancerUtils.getOldLoadBalancers(loadBalancers);
        if (CollectionUtils.isNullOrEmpty(oldLoadBalancers)) {
            return ruleResult;
        }

        List<LoadBalancer> oldWorkingLoadBalancers = LoadBalancerUtils.getWorkingLoadBalancers(oldLoadBalancers);
        if (CollectionUtils.isNullOrEmpty(oldWorkingLoadBalancers)) {
            return ruleResult;
        }

        analyzeNetworkLbs(ruleResult, oldWorkingLoadBalancers);

        return ruleResult;
    }

    private void analyzeNetworkLbs(RuleResult ruleResult, List<LoadBalancer> loadBalancers) {
        Map<String, LoadBalancer> metricNameMap =
                LoadBalancerUtils.getMetricNameMap(loadBalancers, LoadBalancerTypeEnum.NETWORK);
        Map<String, MetricDataResult> metricResult =
                cloudWatchAccessor.getNetworkLBNewFlowCountMetricData(new ArrayList<>(metricNameMap.keySet()));

        for (Map.Entry<String, MetricDataResult> entry : metricResult.entrySet()) {
            if (MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        metricNameMap.get(entry.getKey()).loadBalancerName(),
                        IssueCode.NETWORK_LOAD_BALANCER_NOT_USED));
            }
        }
    }
}