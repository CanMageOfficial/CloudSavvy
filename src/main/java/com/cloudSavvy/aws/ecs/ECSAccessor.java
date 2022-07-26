package com.cloudSavvy.aws.ecs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ClusterField;
import software.amazon.awssdk.services.ecs.model.DescribeClustersRequest;
import software.amazon.awssdk.services.ecs.model.DescribeClustersResponse;
import software.amazon.awssdk.services.ecs.model.DescribeServicesRequest;
import software.amazon.awssdk.services.ecs.model.DescribeServicesResponse;
import software.amazon.awssdk.services.ecs.model.ListServicesResponse;
import software.amazon.awssdk.services.ecs.model.Service;
import software.amazon.awssdk.services.ecs.paginators.ListServicesIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ECSAccessor {

    public static final String FAILED = "FAILED";
    public static final String ACTIVE = "ACTIVE";

    private static final String DEPROVISIONING = "DEPROVISIONING";
    private static final String INACTIVE = "INACTIVE";

    private EcsClient ecsClient;

    public List<Cluster> listClusters() {
        DescribeClustersRequest request = DescribeClustersRequest.builder()
                .include(ClusterField.STATISTICS).build();
        DescribeClustersResponse clustersResponse = ecsClient.describeClusters(request);
        List<Cluster> clusters = clustersResponse.clusters();
        return clusters.stream()
                .filter(cluster -> !DEPROVISIONING.equals(cluster.status())
                        && !INACTIVE.equals(cluster.status())).collect(Collectors.toList());
    }

    public List<String> listServicesArns() {
        ListServicesIterable servicesIterable = ecsClient.listServicesPaginator();
        List<String> services = new ArrayList<>();
        for (ListServicesResponse servicesResponse : servicesIterable) {
            services.addAll(servicesResponse.serviceArns());

            if (services.size() > 1000) {
                break;
            }
        }
        return services;
    }

    public List<Service> describeServices(List<String> serviceArns) {
        DescribeServicesRequest request = DescribeServicesRequest.builder().services(serviceArns).build();
        DescribeServicesResponse servicesIterable = ecsClient.describeServices(request);
        List<Service> services = new ArrayList<>();
        for (Service servicesResponse : servicesIterable.services()) {
            services.add(servicesResponse);

            if (services.size() > 1000) {
                break;
            }
        }
        return services;
    }
}
