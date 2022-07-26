package com.cloudSavvy.aws.kinesis;

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
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;
import software.amazon.awssdk.services.kinesis.model.StreamMode;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class KinesisDataStreamRule implements AnalyzerRule {

    private KinesisAccessor kinesisAccessor;
    private CloudWatchAccessor cloudWatchAccessor;
    private final EntityType entityType = EntityType.KINESIS_DATA_STREAM;

    @Override
    public AWSService getAWSService() {
        return AWSService.Kinesis;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> streamNames = kinesisAccessor.listStreamNames();

        if (CollectionUtils.isNullOrEmpty(streamNames)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, streamNames.stream()
                .map(streamName -> new ResourceMetadata(streamName, null))
                .collect(Collectors.toList())));

        List<StreamDescription> streamDescriptions = Collections.synchronizedList(new ArrayList<>());
        streamNames.stream().parallel().forEach(streamName -> {
                    StreamDescription streamDescription = kinesisAccessor.describeStream(streamName);
                    if (streamDescription.streamStatus() != StreamStatus.DELETING) {
                        streamDescriptions.add(streamDescription);
                    }
                }
        );

        List<StreamDescription> oldStreamDescriptions = streamDescriptions.stream()
                .filter(desc -> TimeUtils.getElapsedTimeInDays(desc.streamCreationTimestamp()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        Map<String, StreamDescription> streamNameDescMap =
                oldStreamDescriptions.stream().collect(Collectors.toMap(StreamDescription::streamName,
                Function.identity()));

        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getKinesisGetRecordRecordsMetricData(new ArrayList<>(streamNameDescMap.keySet()));

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            StreamDescription streamDescription = streamNameDescMap.get(entry.getKey());
            double maxConnection = MetricUtils.getMax(entry.getValue());
            if (maxConnection < 1) {
                if (streamDescription.streamModeDetails().streamMode() == StreamMode.PROVISIONED) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            entry.getKey(), IssueCode.KINESIS_DATA_STREAM_PROVISIONED_NOT_USED));
                } else {
                    ruleResult.addIssueData(new IssueData(entityType,
                            entry.getKey(), IssueCode.KINESIS_DATA_STREAM_NOT_USED));
                }
            } else if (!CollectionUtils.isNullOrEmpty(streamDescription.shards())
                    && streamDescription.shards().size() > 1) {
                // 24 * 60 = 1440
                if (maxConnection < 1440 * streamDescription.shards().size()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            entry.getKey(), IssueCode.PROVISIONED_KINESIS_DATA_STREAM_HAS_LOW_USAGE));
                }
            }
        }

        return ruleResult;
    }
}