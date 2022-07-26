package com.cloudSavvy.aws.elasticache;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class MemCachedClusterRule implements AnalyzerRule {

    private ElastiCacheAccessor elastiCacheAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.ELASTICACHE_MEMCACHED_CLUSTER;

    @Override
    public AWSService getAWSService() {
        return AWSService.ElastiCache;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<CacheCluster> clusters = elastiCacheAccessor.listMemCachedClusters();
        if (CollectionUtils.isNullOrEmpty(clusters)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, clusters.stream()
                .map(cluster -> new ResourceMetadata(cluster.cacheClusterId(), cluster.cacheClusterCreateTime()))
                .collect(Collectors.toList())));

        List<CacheCluster> oldClusters = clusters.stream()
                .filter(cluster -> TimeUtils.getElapsedTimeInDays(cluster.cacheClusterCreateTime()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(oldClusters)) {
            return ruleResult;
        }

        List<String> oldCacheClusterIds = oldClusters.stream()
                .map(CacheCluster::cacheClusterId).collect(Collectors.toList());

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getElastiCacheClusterNewConnectionsMetricData(oldCacheClusterIds);

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            if (MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.ELASTICACHE_MEMCACHED_CLUSTER_NOT_USED));
            }
        }

        return ruleResult;
    }
}
