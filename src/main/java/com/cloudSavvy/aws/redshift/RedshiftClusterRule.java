package com.cloudSavvy.aws.redshift;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RedshiftClusterRule implements AnalyzerRule {

    private final EntityType entityType = EntityType.REDSHIFT_CLUSTER;

    private CloudWatchAccessor cloudWatchAccessor;

    private static final String STORAGE_FULL = "storage-full";
    private static final String INCOMPATIBLE_PARAMETERS = "incompatible-parameters";
    private static final String INCOMPATIBLE_NETWORK = "incompatible-network";
    private static final String INCOMPATIBLE_HSM = "incompatible-hsm";

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_Redshift;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Cluster> clusters = ruleContext.getRedshiftClusters();

        if (CollectionUtils.isNullOrEmpty(clusters)) {
            return ruleResult;
        }

        for (Cluster cluster : clusters) {
            if (STORAGE_FULL.equalsIgnoreCase(cluster.clusterStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, cluster.clusterIdentifier(),
                        IssueCode.Redshift_CLUSTER_STORAGE_FULL));
            } else if (INCOMPATIBLE_NETWORK.equalsIgnoreCase(cluster.clusterStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, cluster.clusterIdentifier(),
                        IssueCode.Redshift_CLUSTER_INCOMPATIBLE_NETWORK));
            } else if (INCOMPATIBLE_PARAMETERS.equalsIgnoreCase(cluster.clusterStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, cluster.clusterIdentifier(),
                        IssueCode.Redshift_CLUSTER_INCOMPATIBLE_PARAMETERS));
            } else if (INCOMPATIBLE_HSM.equalsIgnoreCase(cluster.clusterStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, cluster.clusterIdentifier(),
                        IssueCode.Redshift_CLUSTER_INCOMPATIBLE_HSM));
            }
        }

        Map<String, Cluster> clusterIdMap =
                clusters.stream()
                        .filter(cluster -> TimeUtils.getElapsedTimeInDays(cluster.clusterCreateTime()) > ResourceAge.SEVEN_DAYS)
                        .collect(Collectors.toMap(Cluster::clusterIdentifier, Function.identity()));

        ruleResult.addServiceData(new ServiceData(entityType, clusters.stream()
                .map(cluster -> new ResourceMetadata(cluster.clusterIdentifier(), cluster.clusterCreateTime()))
                .collect(Collectors.toList())));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRedshiftClusterDbConnectionsMetricData(new ArrayList<>(clusterIdMap.keySet()));
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxComputeSeconds = MetricUtils.getMax(entry.getValue());
            if (maxComputeSeconds < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.REDSHIFT_CLUSTER_NOT_USED));
            }
        }

        return ruleResult;
    }
}
