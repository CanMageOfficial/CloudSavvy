package com.cloudSavvy.aws.eks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterResponse;
import software.amazon.awssdk.services.eks.model.ListClustersResponse;
import software.amazon.awssdk.services.eks.model.ListFargateProfilesRequest;
import software.amazon.awssdk.services.eks.model.ListFargateProfilesResponse;
import software.amazon.awssdk.services.eks.model.ListNodegroupsRequest;
import software.amazon.awssdk.services.eks.model.ListNodegroupsResponse;
import software.amazon.awssdk.services.eks.paginators.ListClustersIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class EKSAccessor {

    private EksClient eksClient;

    public List<String> listClusters() {
        ListClustersIterable clustersIterable = eksClient.listClustersPaginator();
        List<String> clusters = new ArrayList<>();
        for (ListClustersResponse clustersResponse : clustersIterable) {
            clusters.addAll(clustersResponse.clusters());

            if (clusters.size() > 1000) {
                break;
            }
        }
        return clusters;
    }

    public Cluster getCluster(final String clusterName) {
        DescribeClusterRequest request = DescribeClusterRequest.builder().name(clusterName).build();
        DescribeClusterResponse clusterResponse = eksClient.describeCluster(request);
        return clusterResponse.cluster();
    }

    public int getNodeGroupsCount(final String clusterName) {
        ListNodegroupsRequest request = ListNodegroupsRequest.builder().clusterName(clusterName).build();
        ListNodegroupsResponse response = eksClient.listNodegroups(request);
        return response.nodegroups().size();
    }

    public int getFargateProfileCount(final String clusterName) {
        ListFargateProfilesRequest request = ListFargateProfilesRequest.builder().clusterName(clusterName).build();
        ListFargateProfilesResponse response = eksClient.listFargateProfiles(request);
        return response.fargateProfileNames().size();
    }
}
