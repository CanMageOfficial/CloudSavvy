package com.cloudSavvy.aws.autoscaling;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClient;
import software.amazon.awssdk.services.applicationautoscaling.model.DescribeScalableTargetsRequest;
import software.amazon.awssdk.services.applicationautoscaling.model.DescribeScalableTargetsResponse;
import software.amazon.awssdk.services.applicationautoscaling.model.ScalableDimension;
import software.amazon.awssdk.services.applicationautoscaling.model.ScalableTarget;
import software.amazon.awssdk.services.applicationautoscaling.model.ServiceNamespace;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class AutoScalingAccessor {
    private ApplicationAutoScalingClient autoScalingClient;

    public Map<String, ScalableTarget> getScalableTargets(List<String> resourceNames,
                                                          ScalableDimension scalableDimension) {
        Map<String, ScalableTarget> resourceNameTargets = new HashMap<>();
        if (CollectionUtils.isNullOrEmpty(resourceNames)) {
            return resourceNameTargets;
        }

        Collection<String> resourceIDs = resourceNames.stream()
                .map(resourceName -> buildResourceId(resourceName, scalableDimension))
                .collect(Collectors.toCollection(TreeSet::new));
        DescribeScalableTargetsRequest request =
                DescribeScalableTargetsRequest.builder()
                        .serviceNamespace(getServiceNamespace(scalableDimension))
                        .resourceIds(resourceIDs)
                        .scalableDimension(scalableDimension).build();
        DescribeScalableTargetsResponse response = autoScalingClient.describeScalableTargets(request);
        List<ScalableTarget> targets = response.scalableTargets();
        for (ScalableTarget target : targets) {
            resourceNameTargets.put(parseResourceId(target.resourceId()), target);
        }

        return resourceNameTargets;
    }

    public String buildResourceId(String resourceName, ScalableDimension scalableDimension) {
        switch (scalableDimension) {
            case DYNAMODB_TABLE_READ_CAPACITY_UNITS:
            case DYNAMODB_TABLE_WRITE_CAPACITY_UNITS:
                return "table/" + resourceName;
            case LAMBDA_FUNCTION_PROVISIONED_CONCURRENCY:
                return "lambda/" + resourceName;
            default:
                throw new IllegalArgumentException("invalid dimension: " + scalableDimension);
        }
    }

    // https://docs.aws.amazon.com/sdk-for-ruby/v2/api/Aws/ApplicationAutoScaling/Types/DescribeScalableTargetsRequest.html#resource_ids-instance_method
    public ServiceNamespace getServiceNamespace(ScalableDimension scalableDimension) {
        switch (scalableDimension) {
            case DYNAMODB_TABLE_READ_CAPACITY_UNITS:
            case DYNAMODB_TABLE_WRITE_CAPACITY_UNITS:
                return ServiceNamespace.DYNAMODB;
            case LAMBDA_FUNCTION_PROVISIONED_CONCURRENCY:
                return ServiceNamespace.LAMBDA;
            default:
                throw new IllegalArgumentException("invalid dimension: " + scalableDimension);
        }
    }

    public String parseResourceId(@NonNull String resourceID) {
        int index = resourceID.indexOf('/');
        if (index == -1) {
            throw new IllegalArgumentException("resourceID is in invalid format:" + resourceID);
        }

        return resourceID.substring(index + 1);
    }
}
