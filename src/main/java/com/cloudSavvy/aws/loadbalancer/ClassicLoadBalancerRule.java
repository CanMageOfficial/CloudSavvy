package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class ClassicLoadBalancerRule implements AnalyzerRule {

    private LoadBalancerAccessor loadBalancerAccessor;
    private final EntityType entityType = EntityType.LOAD_BALANCER;

    @Override
    public AWSService getAWSService() {
        return AWSService.LOAD_BALANCER;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<LoadBalancerDescription> loadBalancers = loadBalancerAccessor.listClassicLoadBalancers();

        if (CollectionUtils.isNullOrEmpty(loadBalancers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, loadBalancers.stream()
                .map(lb -> new ResourceMetadata(lb.loadBalancerName(), lb.createdTime()))
                .collect(Collectors.toList())));

        for (LoadBalancerDescription lbDescription : loadBalancers) {
            ruleResult.addIssueData(new IssueData(entityType,
                    lbDescription.loadBalancerName(),
                    IssueCode.CLASSIC_LOAD_BALANCER_DEPRECATED));
        }

        return ruleResult;
    }
}