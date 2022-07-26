package com.cloudSavvy.aws.redshift;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.redshift.model.Cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RedshiftClusterUsageRule implements AnalyzerRule {

    private final EntityType entityType = EntityType.REDSHIFT_CLUSTER;

    private CloudWatchAccessor cloudWatchAccessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_Redshift;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Cluster> clusters = ruleContext.getRedshiftClusters();

        Map<String, Cluster> clusterIdMap =
                clusters.stream()
                        .filter(cluster -> TimeUtils.getElapsedTimeInDays(cluster.clusterCreateTime()) > ResourceAge.SEVEN_DAYS)
                        .collect(Collectors.toMap(Cluster::clusterIdentifier, Function.identity()));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRedshiftClusterCPUUtilMetricData(new ArrayList<>(clusterIdMap.keySet()));
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxCPUUtilization = MetricUtils.getMax(entry.getValue());
            if (maxCPUUtilization < 20) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.REDSHIFT_CLUSTER_UNDER_UTILIZED));
            }
        }

        return ruleResult;
    }
}
