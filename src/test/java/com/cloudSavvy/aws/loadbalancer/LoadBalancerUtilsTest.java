package com.cloudSavvy.aws.loadbalancer;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerState;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadBalancerUtilsTest {
    private static final String ELB_ARN_ROOT = "arn:aws:elasticloadbalancing:us-east-1:771793231253:";

    @Test
    public void test_getLBMetricName() {
        String metricName =
                LoadBalancerUtils.getLBMetricName(ELB_ARN_ROOT + "loadbalancer/app/testLoadBalancer/3fc7c53ce3b32fbf");
        assertEquals("app/testLoadBalancer/3fc7c53ce3b32fbf", metricName);
    }

    @Test
    public void test_getLBMetricName_NoName() {
        String metricName =
                LoadBalancerUtils.getLBMetricName(ELB_ARN_ROOT + "loadbalancer/");
        assertEquals("", metricName);
    }

    @Test
    public void test_getWorkingLoadBalancers() {
        List<LoadBalancer> loadBalancers = new ArrayList<>();
        loadBalancers.add(buildLoadBalancer(LoadBalancerTypeEnum.APPLICATION, LoadBalancerStateEnum.ACTIVE_IMPAIRED));
        loadBalancers.add(buildLoadBalancer(LoadBalancerTypeEnum.APPLICATION, LoadBalancerStateEnum.ACTIVE));
        loadBalancers.add(buildLoadBalancer(LoadBalancerTypeEnum.NETWORK, LoadBalancerStateEnum.ACTIVE));
        List<LoadBalancer> workingLoadBalancers = LoadBalancerUtils.getWorkingLoadBalancers(loadBalancers);
        assertEquals(2, workingLoadBalancers.size());
        assertEquals(LoadBalancerStateEnum.ACTIVE, workingLoadBalancers.get(0).state().code());
    }

    private LoadBalancer buildLoadBalancer(LoadBalancerTypeEnum type, LoadBalancerStateEnum state) {
        return LoadBalancer.builder().type(type)
                .state(LoadBalancerState.builder().code(state).build())
                .createdTime(Instant.now()).build();
    }
}
