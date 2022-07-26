package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import java.util.List;

@AllArgsConstructor
public class LoadBalancerRuleManager implements RuleManager {
    private LoadBalancerAccessor loadBalancerAccessor;

    public RuleContext setup(RunMetadata runMetadata) {
        List<LoadBalancer> v2LoadBalancers = loadBalancerAccessor.listV2LoadBalancers();
        return RuleContext.builder().v2LoadBalancers(v2LoadBalancers).build();
    }
}
