package com.cloudSavvy.aws.elasticache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersRequest;
import software.amazon.awssdk.services.elasticache.model.ReplicationGroup;
import software.amazon.awssdk.services.elasticache.paginators.DescribeCacheClustersIterable;
import software.amazon.awssdk.services.elasticache.paginators.DescribeReplicationGroupsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class ElastiCacheAccessor {

    private ElastiCacheClient elastiCacheClient;

    private static final String DELETING = "deleting";
    private static final String DELETED = "deleted";
    private static final String CREATING = "creating";
    private static final String MEMCACHED = "memcached";

    public List<CacheCluster> listMemCachedClusters() {
        DescribeCacheClustersRequest request = DescribeCacheClustersRequest.builder()
                .showCacheNodeInfo(true).showCacheClustersNotInReplicationGroups(true).build();
        DescribeCacheClustersIterable dbClustersIterable = elastiCacheClient.describeCacheClustersPaginator(request);
        List<CacheCluster> cacheClusters = new ArrayList<>();
        for (CacheCluster cluster : dbClustersIterable.cacheClusters()) {
            if (!DELETED.equalsIgnoreCase(cluster.cacheClusterStatus())
                    && !DELETING.equalsIgnoreCase(cluster.cacheClusterStatus())
                    && !CREATING.equalsIgnoreCase(cluster.cacheClusterStatus())
                    && MEMCACHED.equals(cluster.engine())) {
                cacheClusters.add(cluster);
            }

            if (cacheClusters.size() > 1000) {
                break;
            }
        }
        return cacheClusters;
    }

    public List<ReplicationGroup> listRedisReplicationGroups() {
        DescribeReplicationGroupsIterable replicationGroupsIterable = elastiCacheClient.describeReplicationGroupsPaginator();
        List<ReplicationGroup> replicationGroups = new ArrayList<>();
        for (ReplicationGroup replicationGroup : replicationGroupsIterable.replicationGroups()) {
            if (!DELETING.equalsIgnoreCase(replicationGroup.status())
                    && !CREATING.equalsIgnoreCase(replicationGroup.status())) {
                replicationGroups.add(replicationGroup);
            }

            if (replicationGroups.size() > 1000) {
                break;
            }
        }
        return replicationGroups;
    }
}
