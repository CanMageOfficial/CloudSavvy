package com.cloudSavvy.aws.redshift;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.paginators.DescribeClustersIterable;
import software.amazon.awssdk.services.redshiftserverless.RedshiftServerlessClient;
import software.amazon.awssdk.services.redshiftserverless.model.ListWorkgroupsRequest;
import software.amazon.awssdk.services.redshiftserverless.model.Workgroup;
import software.amazon.awssdk.services.redshiftserverless.model.WorkgroupStatus;
import software.amazon.awssdk.services.redshiftserverless.paginators.ListWorkgroupsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RedshiftAccessor {

    private static final String DELETING = "deleting";
    private static final String FINAL_SNAPSHOT = "final-snapshot";
    private static final String CREATING = "creating";

    private RedshiftServerlessClient redshiftServerlessClient;
    private RedshiftClient redshiftClient;

    public List<Workgroup> listServerlessWorkgroups() {
        ListWorkgroupsRequest request = ListWorkgroupsRequest.builder().build();
        ListWorkgroupsIterable workgroupsIterable = redshiftServerlessClient.listWorkgroupsPaginator(request);
        List<Workgroup> workgroups = new ArrayList<>();
        for (Workgroup workgroup : workgroupsIterable.workgroups()) {
            if (workgroup.status() != WorkgroupStatus.DELETING && workgroup.status() != WorkgroupStatus.CREATING) {
                workgroups.add(workgroup);
            }

            if (workgroups.size() > 1000) {
                break;
            }
        }
        return workgroups;
    }

    public List<Cluster> listClusters() {
        DescribeClustersIterable clustersIterable = redshiftClient.describeClustersPaginator();
        List<Cluster> clusters = new ArrayList<>();
        for (Cluster cluster : clustersIterable.clusters()) {
            if (!DELETING.equalsIgnoreCase(cluster.clusterStatus())
                    && !CREATING.equalsIgnoreCase(cluster.clusterStatus())
                    && !FINAL_SNAPSHOT.equalsIgnoreCase(cluster.clusterStatus())) {
                clusters.add(cluster);
            }

            if (clusters.size() > 1000) {
                break;
            }
        }
        return clusters;
    }
}
