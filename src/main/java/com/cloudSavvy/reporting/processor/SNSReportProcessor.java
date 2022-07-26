package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.EnvironmentUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@AllArgsConstructor
public class SNSReportProcessor implements ReportProcessor {

    private SnsClient snsClient;

    @Override
    public ProcessReportResult processReport(final @NonNull ProcessReportInput input) {
        String snsTopicArn = EnvironmentUtils.getNotificationTopicArn();
        if (snsTopicArn == null) {
            log.error("No {} is set in environment variables.", EnvironmentUtils.NOTIFICATION_TOPIC_ARN);
            return ProcessReportResult.builder().build();
        }

        if (!input.getReportTypeDataMap().containsKey(ReportType.SHORT_MESSAGE)) {
            throw new RuntimeException("Short message in reports is missing");
        }

        String shortMessage = input.getReportTypeDataMap().get(ReportType.SHORT_MESSAGE).getReport();

        // This is special logic to show there are errors during run
        String detailedReportMessage = input.getReportTypeDataMap().containsKey(ReportType.ERROR_DATA_HTML)
                ? "= " : ": ";
        String snsMessage = shortMessage + " Detailed Report" + detailedReportMessage + input.outputBucketName
                + " (S3 Bucket), " + input.outputFolderName + " (Folder)";
        publishMessage(snsMessage, snsTopicArn);
        log.info("Notification is sent to the topic: {}", snsTopicArn);
        return ProcessReportResult.builder().reportLocationType(ReportLocationType.SNS_TOPIC)
                .snsTopicArn(snsTopicArn).build();
    }

    private void publishMessage(String message, String topicArn) {
        PublishRequest request = PublishRequest.builder()
                .message(message)
                .topicArn(topicArn)
                .build();

        snsClient.publish(request);
    }

}
