package com.cloudSavvy.aws.ecs;

import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.Service;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ECSUtils {
    public static String getClusterName(String clusterArn) {
        if (StringUtils.isEmpty(clusterArn)) {
            return null;
        }

        int index = clusterArn.lastIndexOf("/");
        if (index == -1) {
            return null;
        }

        return clusterArn.substring(index + 1);
    }

    public static int getActiveServiceCount(List<Cluster> clusters) {
        return clusters.stream().filter(cluster -> cluster.activeServicesCount() != null)
                .mapToInt(Cluster::activeServicesCount).sum();
    }

    public static Map<String, List<Service>> getClusterNameToServicesMap(List<Service> services) {
        Map<String, List<Service>> result = new HashMap<>();
        Map<String, String> clusterArnToNameMap = new HashMap<>();
        for (Service service : services) {
            if (!clusterArnToNameMap.containsKey(service.clusterArn())) {
                String clusterName = getClusterName(service.clusterArn());
                if (clusterName != null) {
                    List<Service> serviceList = result.getOrDefault(clusterName, new ArrayList<>());
                    serviceList.add(service);
                    result.put(clusterName, serviceList);
                }
                clusterArnToNameMap.put(service.clusterArn(), clusterName);
            } else {
                if (clusterArnToNameMap.get(service.clusterArn()) != null) {
                    result.get(clusterArnToNameMap.get(service.clusterArn())).add(service);
                }
            }
        }
        return result;
    }
}
