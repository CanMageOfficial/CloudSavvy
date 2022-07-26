package com.cloudSavvy.aws.kinesis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2Client;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationDetail;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationMode;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationStatus;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ApplicationSummary;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.DescribeApplicationRequest;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.DescribeApplicationResponse;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ListApplicationsRequest;
import software.amazon.awssdk.services.kinesisanalyticsv2.model.ListApplicationsResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class KinesisAccessor {

    private KinesisClient kinesisClient;
    private KinesisAnalyticsV2Client kinesisAnalyticsV2Client;

    public List<String> listStreamNames() {
        ListStreamsResponse listStreamsResponse = kinesisClient.listStreams();
        List<String> streamNames = listStreamsResponse.streamNames();
        log.debug("Kinesis stream names count: {}", streamNames.size());
        return streamNames;
    }

    public StreamDescription describeStream(String streamName) {
        DescribeStreamRequest request = DescribeStreamRequest.builder()
                .streamName(streamName).build();
        DescribeStreamResponse streamResponse = kinesisClient.describeStream(request);
        return streamResponse.streamDescription();
    }

    public List<ApplicationSummary> listAnalyticApplicationsV2() {
        String token = null;
        List<ApplicationSummary> appSummaries = new ArrayList<>();
        do {
            ListApplicationsRequest request = ListApplicationsRequest.builder().nextToken(token).build();
            ListApplicationsResponse listApplicationsResponse = kinesisAnalyticsV2Client.listApplications(request);
            for (ApplicationSummary summary : listApplicationsResponse.applicationSummaries()) {
                if (summary.applicationMode() == ApplicationMode.INTERACTIVE
                        && summary.applicationStatus() != ApplicationStatus.DELETING) {
                    appSummaries.add(summary);
                }
            }
            token = listApplicationsResponse.nextToken();
        } while (token != null && appSummaries.size() < 1000);
        return appSummaries;
    }

    public ApplicationDetail describeAnalyticApplicationV2(String appName) {
        DescribeApplicationRequest request = DescribeApplicationRequest.builder()
                .applicationName(appName).build();
        DescribeApplicationResponse describeApplication = kinesisAnalyticsV2Client.describeApplication(request);
        return describeApplication.applicationDetail();
    }
}
