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
import com.cloudSavvy.aws.metric.ElastiCacheShardMetricDimension;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.elasticache.model.NodeGroup;
import software.amazon.awssdk.services.elasticache.model.ReplicationGroup;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class RedisClusterRule implements AnalyzerRule {

    private ElastiCacheAccessor elastiCacheAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.ELASTICACHE_REDIS_CLUSTER;

    @Override
    public AWSService getAWSService() {
        return AWSService.ElastiCache;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<ReplicationGroup> replicationGroups = elastiCacheAccessor.listRedisReplicationGroups();

        if (CollectionUtils.isNullOrEmpty(replicationGroups)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, replicationGroups.stream()
                .map(rg -> new ResourceMetadata(rg.replicationGroupId(), rg.replicationGroupCreateTime()))
                .collect(Collectors.toList())));

        List<ReplicationGroup> oldReplicationGroups = replicationGroups.stream()
                .filter(rg -> TimeUtils.getElapsedTimeInDays(rg.replicationGroupCreateTime()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldReplicationGroups)) {
            return ruleResult;
        }

        List<String> replicationGroupsIds = new ArrayList<>();
        for (ReplicationGroup repGroup : oldReplicationGroups) {
            for (NodeGroup ng : repGroup.nodeGroups()) {
                replicationGroupsIds.add(getDimensionGroupId(repGroup, ng));
            }
        }

        Map<String, MetricDataResult> cpuMetricDataMap =
                cloudWatchAccessor.getElastiCacheShardCPUUtilMetricData(replicationGroupsIds);
        Map<String, MetricDataResult> memoryMetricDataMap =
                cloudWatchAccessor.getElastiCacheShardMemoryMetricData(replicationGroupsIds);

        for (ReplicationGroup repGroup : oldReplicationGroups) {
            boolean highUsage = false;
            for (NodeGroup ng : repGroup.nodeGroups()) {
                String dimGroupId = getDimensionGroupId(repGroup, ng);
                if (cpuMetricDataMap.containsKey(dimGroupId)) {
                    if (MetricUtils.getMax(cpuMetricDataMap.get(dimGroupId)) >= 10) {
                        highUsage = true;
                    }
                }
                if (memoryMetricDataMap.containsKey(dimGroupId)) {
                    if (MetricUtils.getMax(memoryMetricDataMap.get(dimGroupId)) >= 10) {
                        highUsage = true;
                    }
                }
            }

            if (!highUsage) {
                ruleResult.addIssueData(new IssueData(entityType, repGroup.replicationGroupId(),
                        IssueCode.ELASTICACHE_REDIS_CLUSTER_HAS_LOW_USAGE));
            }
        }
        return ruleResult;
    }

    private String getDimensionGroupId(ReplicationGroup repGroup, NodeGroup nodeGroup) {
        return ElastiCacheShardMetricDimension.buildDimensionGroup(repGroup.replicationGroupId(), nodeGroup.nodeGroupId());
    }
}
