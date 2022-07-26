package com.cloudSavvy.aws.dynamodb;

import com.cloudSavvy.aws.autoscaling.AutoScalingAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.applicationautoscaling.model.ScalableDimension;
import software.amazon.awssdk.services.applicationautoscaling.model.ScalableTarget;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputDescription;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class DynamoDbTableUsageRule implements AnalyzerRule {

    private DynamoDbAccessor dynamoDbAccessor;

    private AutoScalingAccessor autoScalingAccessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.DynamoDB;
    }

    private final EntityType entityType = EntityType.DynamoDB_TABLE;

    @Override
    public RuleResult call(final RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<String> tableNames = ruleContext.getDynamoDbTableNames();
        if (CollectionUtils.isNullOrEmpty(tableNames)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, tableNames.stream()
                .map(tableName -> new ResourceMetadata(tableName, null))
                .collect(Collectors.toList())));

        Map<String, ScalableTarget> writeCapacityDimensions =
                autoScalingAccessor.getScalableTargets(tableNames, ScalableDimension.DYNAMODB_TABLE_WRITE_CAPACITY_UNITS);
        Map<String, ScalableTarget> readCapacityDimensions =
                autoScalingAccessor.getScalableTargets(tableNames, ScalableDimension.DYNAMODB_TABLE_READ_CAPACITY_UNITS);
        Map<String, TableDescription> tableDescriptionMap = new ConcurrentHashMap<>();
        tableNames.stream().parallel().forEach(tableName -> {
            TableDescription description = dynamoDbAccessor.getTableDescription(tableName);
            tableDescriptionMap.put(tableName, description);
        });

        for (Map.Entry<String, TableDescription> entry : tableDescriptionMap.entrySet()) {
            log.debug("analyzing the table: {}", entry.getKey());
            TableDescription description = entry.getValue();
            String tableName = entry.getKey();

            if (description.billingModeSummary() == null
                    || description.billingModeSummary().billingMode() == BillingMode.PROVISIONED) {
                ProvisionedThroughputDescription provisionedThroughput = description.provisionedThroughput();
                log.debug("provisionedThroughput: {}", provisionedThroughput);
                if (provisionedThroughput == null) {
                    continue;
                }

                if (provisionedThroughput.readCapacityUnits() > 1 && !readCapacityDimensions.containsKey(tableName)) {
                    ruleResult.addIssueData(new IssueData(
                            entityType,
                            tableName,
                            IssueCode.TABLE_NO_READ_AUTO_SCALE));
                }

                if (provisionedThroughput.writeCapacityUnits() > 1 && !writeCapacityDimensions.containsKey(tableName)) {
                    ruleResult.addIssueData(new IssueData(
                            entityType,
                            tableName,
                            IssueCode.TABLE_NO_WRITE_AUTO_SCALE));
                }
            }
        }
        return ruleResult;
    }
}
