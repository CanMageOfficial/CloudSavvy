package com.cloudSavvy.aws.cloudwatch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OrderBy;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogGroupsIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class CloudWatchLogAccessor {

    private CloudWatchLogsClient logsClient;

    public List<LogGroup> getNoRetentionUsedLogGroups(int limit) {
        List<LogGroup> logGroups = new ArrayList<>();
        DescribeLogGroupsIterable response = logsClient.describeLogGroupsPaginator();
        for (LogGroup logGroup : response.logGroups()) {
            if (logGroup.retentionInDays() == null && logGroup.storedBytes() > 0) {
                logGroups.add(logGroup);
            }
            if (logGroups.size() >= limit) {
                break;
            }
        }
        return logGroups;
    }

    public Optional<LogGroup> getLogGroup(String logGroupName) {
        DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
                .logGroupNamePrefix(logGroupName).build();
        DescribeLogGroupsResponse response = logsClient.describeLogGroups(request);
        return response.logGroups().stream()
                .filter(lg -> lg.logGroupName().equalsIgnoreCase(logGroupName)).findFirst();
    }

    public List<LogGroup> getLogGroups(String logGroupNamePrefix) {
        DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
                .logGroupNamePrefix(logGroupNamePrefix).build();
        DescribeLogGroupsResponse response = logsClient.describeLogGroups(request);
        return response.logGroups();
    }

    public Optional<LogStream> getLogStream(String logGroupName, String logStreamName) {
        DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName).logStreamNamePrefix(logStreamName).build();
        DescribeLogStreamsResponse response = logsClient.describeLogStreams(request);
        return response.logStreams().stream()
                .filter(ls -> ls.logStreamName().equalsIgnoreCase(logStreamName)).findFirst();
    }

    public Optional<LogStream> getLogStreamByEventTime(String logGroupName) {
        DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .orderBy(OrderBy.LAST_EVENT_TIME).descending(true).limit(1).build();
        DescribeLogStreamsResponse response = logsClient.describeLogStreams(request);
        return response.logStreams().stream().findFirst();
    }
}
