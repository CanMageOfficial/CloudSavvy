package com.cloudSavvy.aws.dynamodb;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.paginators.ListTablesIterable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class DynamoDbAccessor {
    private DynamoDbClient dynamoDbClient;

    public List<String> listTables() {
        List<String> tableNames = new ArrayList<>();
        ListTablesIterable tablesIterable = dynamoDbClient.listTablesPaginator();
        for (String tableName : tablesIterable.tableNames()) {
            tableNames.add(tableName);
            if (tableNames.size() > 1000) {
                break;
            }
        }
        log.debug("tables count: {}", tableNames.size());
        return tableNames;
    }

    public TableDescription getTableDescription(final String tableName) {
        DescribeTableResponse response =
                dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
        return response.table();
    }
}
