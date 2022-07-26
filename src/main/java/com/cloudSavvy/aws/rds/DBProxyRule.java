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
import software.amazon.awssdk.services.rds.model.DBProxy;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class DBProxyRule implements AnalyzerRule {

    private RDSAccessor rdsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.RDS_DB_PROXY;

    @Override
    public AWSService getAWSService() {
        return AWSService.RDS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DBProxy> dbProxies = rdsAccessor.listDBProxies();

        if (CollectionUtils.isNullOrEmpty(dbProxies)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, dbProxies.stream()
                .map(proxy -> new ResourceMetadata(proxy.dbProxyName(), proxy.createdDate()))
                .collect(Collectors.toList())));

        List<String> oldDbProxyNames = dbProxies.stream()
                .filter(proxy -> TimeUtils.getElapsedTimeInDays(proxy.createdDate()) > ResourceAge.SEVEN_DAYS)
                .map(DBProxy::dbProxyName).collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldDbProxyNames)) {
            return ruleResult;
        }

        Map<String, Integer> targetCountMap = new ConcurrentHashMap<>();
        oldDbProxyNames.stream().parallel().forEach(proxyName -> {
            int count = rdsAccessor.getDbProxyTargetCount(proxyName);
            targetCountMap.put(proxyName, count);
        });

        List<String> proxyNamesWithTarget = new ArrayList<>();
        for (String proxyName : oldDbProxyNames) {
            int count = targetCountMap.get(proxyName);
            if (count == 0) {
                ruleResult.addIssueData(new IssueData(entityType, proxyName,
                        IssueCode.RDS_PROXY_HAS_NO_TARGET));
            } else {
                proxyNamesWithTarget.add(proxyName);
            }
        }

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRDSProxyDBConnectionsMetricData(proxyNamesWithTarget);
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxDBConnectionCount = MetricUtils.getMax(entry.getValue());
            if (maxDBConnectionCount < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.RDS_DB_PROXY_NOT_USED));
            }
        }

        return ruleResult;
    }
}
