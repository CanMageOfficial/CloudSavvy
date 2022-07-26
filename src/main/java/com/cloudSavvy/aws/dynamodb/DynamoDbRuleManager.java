package com.cloudSavvy.aws.dynamodb;

import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DynamoDbRuleManager implements RuleManager {
    private DynamoDbAccessor dynamoDbAccessor;

    public RuleContext setup(RunMetadata runMetadata) {
        List<String> tableNames = dynamoDbAccessor.listTables();
        return RuleContext.builder().dynamoDbTableNames(tableNames).build();
    }
}
