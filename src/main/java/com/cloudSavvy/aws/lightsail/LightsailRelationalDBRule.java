package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.services.lightsail.model.RelationalDatabase;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailRelationalDBRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_DATABASE;
    private static final String STOPPED = "stopped";

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<RelationalDatabase> databases = lightsailAccessor.listRelationalDatabases();

        if (CollectionUtils.isNullOrEmpty(databases)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, databases.stream()
                .map(db -> new ResourceMetadata(db.name(), db.createdAt()))
                .collect(Collectors.toList())));

        List<RelationalDatabase> oldDatabases = databases.stream()
                .filter(database -> TimeUtils.getElapsedTimeInDays(database.createdAt()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        List<RelationalDatabase> oldWorkingDatabases = new ArrayList<>();
        for (RelationalDatabase database : oldDatabases) {
            if (STOPPED.equals(database.state())) {
                ruleResult.addIssueData(new IssueData(entityType,
                        database.name(), IssueCode.LIGHTSAIL_DATABASE_IS_STOPPED));
            } else {
                oldWorkingDatabases.add(database);
            }
        }

        if (CollectionUtils.isNullOrEmpty(oldWorkingDatabases)) {
            return ruleResult;
        }

        oldWorkingDatabases.stream().parallel().forEach(database -> {
            List<MetricDatapoint> cpuMetricData =
                    lightsailAccessor.getRelationalDBConnectionsMetrics(database.name());
            double max = LightSailUtils.getMaxOfMetricSum(cpuMetricData);
            if (max < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        database.name(), IssueCode.LIGHTSAIL_DATABASE_IS_NOT_USED));
            }
        });

        return ruleResult;
    }
}
