package com.cloudSavvy.aws.memorydb;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.memorydb.MemoryDbClient;
import software.amazon.awssdk.services.memorydb.model.Cluster;
import software.amazon.awssdk.services.memorydb.model.DescribeClustersRequest;
import software.amazon.awssdk.services.memorydb.model.DescribeClustersResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class MemoryDbAccessor {

    private MemoryDbClient memoryDbClient;

    private static final String DELETING = "deleting";
    private static final String DELETED = "deleted";
    private static final String CREATING = "creating";

    public List<Cluster> listClusters() {
        String token = null;
        List<Cluster> clusters = new ArrayList<>();
        do {
            DescribeClustersRequest request = DescribeClustersRequest.builder().nextToken(token)
                    .showShardDetails(true).build();
            DescribeClustersResponse clustersResponse = memoryDbClient.describeClusters(request);

            for (Cluster cluster : clustersResponse.clusters()) {
                if (!DELETED.equalsIgnoreCase(cluster.status()) && !DELETING.equalsIgnoreCase(cluster.status())
                        && !CREATING.equalsIgnoreCase(cluster.status())) {
                    clusters.add(cluster);
                }
            }
            token = clustersResponse.nextToken();
        } while (token != null && clusters.size() < 1000);
        return clusters;
    }
}
