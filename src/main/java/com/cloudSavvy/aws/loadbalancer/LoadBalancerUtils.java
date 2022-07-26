package com.cloudSavvy.aws.loadbalancer;

import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.ResourceAge;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoadBalancerUtils {
    public static Map<String, LoadBalancer> getMetricNameMap(List<LoadBalancer> loadBalancers,
                                                             LoadBalancerTypeEnum loadBalancerType) {
        return loadBalancers.stream()
                .filter(lb -> lb.type() == loadBalancerType)
                .collect(Collectors.toMap(lb -> getLBMetricName(lb.loadBalancerArn()), Function.identity()));
    }

    public static String getLBMetricName(String loadBalancerArn) {
        if (StringUtils.isEmpty(loadBalancerArn)) {
            return null;
        }

        String[] tokens = loadBalancerArn.split(":");

        if (tokens.length < 2) {
            return null;
        }

        int separator = tokens[tokens.length - 1].indexOf('/');
        if (separator == -1) {
            return null;
        }
        return tokens[tokens.length - 1].substring(separator + 1);
    }

    public static List<LoadBalancer> filterLoadBalancers(List<LoadBalancer> loadBalancers,
                                                    LoadBalancerTypeEnum type) {
        return loadBalancers.stream()
                .filter(lb -> lb.type() == type).collect(Collectors.toList());
    }

    public static List<LoadBalancer> getOldLoadBalancers(List<LoadBalancer> loadBalancers) {
        return loadBalancers.stream()
                .filter(lb -> TimeUtils.getElapsedTimeInDays(lb.createdTime()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());
    }

    public static List<LoadBalancer> getWorkingLoadBalancers(List<LoadBalancer> loadBalancers) {
        return loadBalancers.stream()
                .filter(lb -> lb.state().code() != LoadBalancerStateEnum.FAILED
                        && lb.state().code() != LoadBalancerStateEnum.ACTIVE_IMPAIRED)
                .collect(Collectors.toList());
    }
}
