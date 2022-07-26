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
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class DBInstanceRule implements AnalyzerRule {

    private RDSAccessor rdsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.RDS_DB_INSTANCE;

    // https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/accessing-monitoring.html
    private static final String STORAGE_FULL = "storage-full";
    private static final String STOPPED = "stopped";
    private static final String INCOMPATIBLE_PARAMETERS = "incompatible-parameters";
    private static final String INCOMPATIBLE_OPTION_GROUP = "incompatible-option-group";

    @Override
    public AWSService getAWSService() {
        return AWSService.RDS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DBInstance> dbInstances = rdsAccessor.listNonClusterDBInstances();

        if (CollectionUtils.isNullOrEmpty(dbInstances)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, dbInstances.stream()
                .map(instance -> new ResourceMetadata(instance.dbInstanceIdentifier(), instance.instanceCreateTime()))
                .collect(Collectors.toList())));

        for (DBInstance instance : dbInstances) {
            if (STORAGE_FULL.equalsIgnoreCase(instance.dbInstanceStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, instance.dbInstanceIdentifier(),
                        IssueCode.RDS_DB_INSTANCE_STORAGE_FULL));
            } else if (STOPPED.equalsIgnoreCase(instance.dbInstanceStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, instance.dbInstanceIdentifier(),
                        IssueCode.RDS_DB_INSTANCE_STOPPED));
            } else if (INCOMPATIBLE_PARAMETERS.equalsIgnoreCase(instance.dbInstanceStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, instance.dbInstanceIdentifier(),
                        IssueCode.RDS_DB_INSTANCE_INCOMPATIBLE_PARAMETERS));
            } else if (INCOMPATIBLE_OPTION_GROUP.equalsIgnoreCase(instance.dbInstanceStatus())) {
                ruleResult.addIssueData(new IssueData(entityType, instance.dbInstanceIdentifier(),
                        IssueCode.RDS_DB_INSTANCE_INCOMPATIBLE_OPTION_GROUP));
            }
        }

        List<String> oldDBInstanceIds = dbInstances.stream()
                .filter(dbInstance -> TimeUtils.getElapsedTimeInDays(dbInstance.instanceCreateTime()) > ResourceAge.SEVEN_DAYS)
                .map(DBInstance::dbInstanceIdentifier).collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(oldDBInstanceIds)) {
            return ruleResult;
        }

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRDSInstanceDBConnectionsMetricData(oldDBInstanceIds);
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxDBConnectionCount = MetricUtils.getMax(entry.getValue());
            if (maxDBConnectionCount < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.RDS_DB_INSTANCE_NOT_USED));
            }
        }
        return ruleResult;
    }
}
