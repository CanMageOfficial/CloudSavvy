package com.cloudSavvy.aws.memorydb;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.memorydb.model.Cluster;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class MemoryDbClusterRule implements AnalyzerRule {

    private MemoryDbAccessor memoryDbAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.MEMORY_DB_CLUSTER;

    @Override
    public AWSService getAWSService() {
        return AWSService.MEMORY_DB;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Cluster> clusters = memoryDbAccessor.listClusters();

        if (CollectionUtils.isNullOrEmpty(clusters)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, clusters.stream()
                .map(cluster -> new ResourceMetadata(cluster.name(), null))
                .collect(Collectors.toList())));

        List<String> clusterIds = clusters.stream().map(Cluster::name).collect(Collectors.toList());
        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getMemoryDbClusterNewConnectionsMetricData(clusterIds);

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            if (MetricUtils.getMetricSize(entry.getValue()) > ResourceAge.SEVEN_DAYS
                    && MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.MEMORY_DB_CLUSTER_NOT_USED));
            }
        }

        return ruleResult;
    }
}
