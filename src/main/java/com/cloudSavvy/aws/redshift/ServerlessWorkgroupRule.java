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
import software.amazon.awssdk.services.redshiftserverless.model.Workgroup;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ServerlessWorkgroupRule implements AnalyzerRule {

    private final EntityType entityType = EntityType.REDSHIFT_SERVERLESS_WORKGROUP;

    private RedshiftAccessor redshiftAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_Redshift;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Workgroup> workgroups = redshiftAccessor.listServerlessWorkgroups();

        if (CollectionUtils.isNullOrEmpty(workgroups)) {
            return ruleResult;
        }

        Map<String, Workgroup> workgroupNameMap =
                workgroups.stream()
                        .filter(workgroup -> TimeUtils.getElapsedTimeInDays(workgroup.creationDate()) > ResourceAge.SEVEN_DAYS)
                        .collect(Collectors.toMap(Workgroup::workgroupName, Function.identity()));

        ruleResult.addServiceData(new ServiceData(entityType, workgroups.stream()
                .map(workgroup -> new ResourceMetadata(workgroup.workgroupName(), workgroup.creationDate()))
                .collect(Collectors.toList())));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getRedshiftServerlessComputeSecsMetricData(new ArrayList<>(workgroupNameMap.keySet()));
        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            double maxComputeSeconds = MetricUtils.getMax(entry.getValue());
            if (maxComputeSeconds < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.REDSHIFT_SERVERLESS_WORKGROUP_NOT_USED));
            }
        }

        return ruleResult;
    }
}
