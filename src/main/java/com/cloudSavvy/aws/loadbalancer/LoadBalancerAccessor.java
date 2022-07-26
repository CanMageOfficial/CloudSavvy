package com.cloudSavvy.aws.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeLoadBalancersIterable;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeTargetGroupsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class LoadBalancerAccessor {

    private ElasticLoadBalancingV2Client loadBalancingV2Client;
    private ElasticLoadBalancingClient loadBalancingClient;

    public List<LoadBalancer> listV2LoadBalancers() {
        DescribeLoadBalancersIterable balancersIterable = loadBalancingV2Client.describeLoadBalancersPaginator();
        List<LoadBalancer> loadBalancers = new ArrayList<>();

        for (LoadBalancer lb : balancersIterable.loadBalancers()) {
            loadBalancers.add(lb);

            if (loadBalancers.size() > 1000) {
                break;
            }
        }
        log.debug("V2 Load Balancers count: {}", loadBalancers.size());
        return loadBalancers;
    }

    public List<TargetGroup> listTargetGroups() {
        DescribeTargetGroupsIterable targetGroupsIterable = loadBalancingV2Client.describeTargetGroupsPaginator();
        List<TargetGroup> targetGroups = new ArrayList<>();

        for (TargetGroup targetGroup : targetGroupsIterable.targetGroups()) {
            targetGroups.add(targetGroup);
            if (targetGroups.size() > 1000) {
                break;
            }
        }
        log.debug("Target groups count: {}", targetGroups.size());
        return targetGroups;
    }

    public List<TargetHealthDescription> getTargetGroupHealth(String targetGroupArn) {
        DescribeTargetHealthRequest request = DescribeTargetHealthRequest.builder()
                .targetGroupArn(targetGroupArn).build();
        DescribeTargetHealthResponse targetHealthResponse = loadBalancingV2Client.describeTargetHealth(request);
        List<TargetHealthDescription> targetHealthList = targetHealthResponse.targetHealthDescriptions();
        log.debug("Target group health: {}", targetHealthList);
        return targetHealthList;
    }

    public List<LoadBalancerDescription> listClassicLoadBalancers() {
        software.amazon.awssdk.services.elasticloadbalancing.paginators.DescribeLoadBalancersIterable loadBalancersIterable =
                loadBalancingClient.describeLoadBalancersPaginator();
        List<LoadBalancerDescription> loadBalancers = new ArrayList<>();

        for (LoadBalancerDescription lbDesc : loadBalancersIterable.loadBalancerDescriptions()) {
            loadBalancers.add(lbDesc);
            if (loadBalancers.size() > 1000) {
                break;
            }
        }
        log.debug("Classic Load Balancers count: {}", loadBalancers.size());
        return loadBalancers;
    }
}
