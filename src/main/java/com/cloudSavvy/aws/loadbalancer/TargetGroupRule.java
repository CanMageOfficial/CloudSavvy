package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthStateEnum;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Slf4j
public class TargetGroupRule implements AnalyzerRule {

    private LoadBalancerAccessor loadBalancerAccessor;
    private final EntityType entityType = EntityType.EC2_TARGET_GROUP;

    @Override
    public AWSService getAWSService() {
        return AWSService.LOAD_BALANCER;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<TargetGroup> targetGroups = loadBalancerAccessor.listTargetGroups();

        if (CollectionUtils.isNullOrEmpty(targetGroups)) {
            return ruleResult;
        }

        List<TargetGroup> tgsWithLBAndHealthCheck = new ArrayList<>();

        for (TargetGroup targetGroup : targetGroups) {
            if (!CollectionUtils.isNullOrEmpty(targetGroup.loadBalancerArns())) {
                if (!targetGroup.healthCheckEnabled()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            targetGroup.targetGroupName(),
                            IssueCode.TARGET_GROUP_HEALTH_CHECK_NOT_ENABLED));
                } else {
                    tgsWithLBAndHealthCheck.add(targetGroup);
                }
            }
        }

        Map<String, List<TargetHealthDescription>> targetHealthMap = new ConcurrentHashMap<>();
        tgsWithLBAndHealthCheck.parallelStream().forEach(tg -> {
            List<TargetHealthDescription> result = loadBalancerAccessor.getTargetGroupHealth(tg.targetGroupArn());
            targetHealthMap.put(tg.targetGroupName(), result);
        });

        for (Map.Entry<String, List<TargetHealthDescription>> entry : targetHealthMap.entrySet()) {
            long unhealthyTargetCount = entry.getValue().stream()
                    .filter(desc -> desc.targetHealth().state() == TargetHealthStateEnum.UNHEALTHY)
                    .count();
            if (unhealthyTargetCount == entry.getValue().size()) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(),
                        IssueCode.TARGET_GROUP_HAS_UNHEALTHY_TARGETS));
            }
        }

        return ruleResult;
    }
}