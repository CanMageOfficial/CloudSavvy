package com.cloudSavvy.aws.rds;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class DBClusterRule implements AnalyzerRule {

    private RDSAccessor rdsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.RDS_DB_CLUSTER;

    @Override
    public AWSService getAWSService() {
        return AWSService.RDS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DBCluster> dbClusters = rdsAccessor.listDBClusters();

        if (CollectionUtils.isNullOrEmpty(dbClusters)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, dbClusters.stream()
                .map(dbCluster -> new ResourceMetadata(dbCluster.dbClusterIdentifier(), dbCluster.clusterCreateTime()))
                .collect(Collectors.toList())));

        Map<String, DBCluster> dbClusterIdMap =
                dbClusters.stream()
                        .filter(dbCluster -> TimeUtils.getElapsedTimeInDays(dbCluster.clusterCreateTime()) > ResourceAge.SEVEN_DAYS)
                        .collect(Collectors.toMap(DBCluster::dbClusterIdentifier, Function.identity()));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRDSClusterDBConnectionsMetricData(new ArrayList<>(dbClusterIdMap.keySet()));
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxDBConnectionCount = MetricUtils.getMax(entry.getValue());
            if (maxDBConnectionCount < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.RDS_DB_CLUSTER_NOT_USED));
            } else if (maxDBConnectionCount < 1000) {
                DBCluster cluster = dbClusterIdMap.get(entry.getKey());
                if (cluster.dbClusterMembers().size() > 3) {
                    ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                            IssueCode.RDS_DB_CLUSTER_HAS_UNUSED_MEMBERS));
                }
            }
        }
        return ruleResult;
    }
}
