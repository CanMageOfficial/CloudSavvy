package com.cloudSavvy.aws.sqs;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class SqsQueueRule implements AnalyzerRule {

    private SqsAccessor sqsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.SQS_QUEUE;

    @Override
    public AWSService getAWSService() {
        return AWSService.SQS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<String> queueUrls = sqsAccessor.listQueueUrls();
        if (CollectionUtils.isNullOrEmpty(queueUrls)) {
            return ruleResult;
        }

        List<String> queueNames = new ArrayList<>();

        for (String queueUrl : queueUrls) {
            try {
                Map<QueueAttributeName, String> attributes = sqsAccessor.getQueueAttributes(queueUrl);
                String queueName = extractQueueName(queueUrl);
                queueNames.add(queueName);

                int messages = parseInt(attributes.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));
                int inFlight = parseInt(attributes.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE));
                long createdTimestamp = parseLong(attributes.get(QueueAttributeName.CREATED_TIMESTAMP));

                boolean isOldEnough = (Instant.now().getEpochSecond() - createdTimestamp)
                        > (long) ResourceAge.SEVEN_DAYS * 86400;

                if (isDlq(queueName) && messages > 0) {
                    ruleResult.addIssueData(new IssueData(entityType, queueName,
                            IssueCode.SQS_QUEUE_HAS_MESSAGES_IN_DLQ));
                } else if (messages > 0 && inFlight == 0 && isOldEnough) {
                    ruleResult.addIssueData(new IssueData(entityType, queueName,
                            IssueCode.SQS_QUEUE_HAS_STUCK_MESSAGES));
                }
            } catch (Exception e) {
                log.warn("Failed to get attributes for SQS queue {}: {}", queueUrl, e.getMessage());
            }
        }

        ruleResult.addServiceData(new ServiceData(entityType, queueNames.stream()
                .map(name -> new ResourceMetadata(name, null))
                .collect(Collectors.toList())));

        return ruleResult;
    }

    private String extractQueueName(String queueUrl) {
        String[] parts = queueUrl.split("/");
        return parts[parts.length - 1];
    }

    private boolean isDlq(String queueName) {
        String lower = queueName.toLowerCase();
        return lower.contains("dlq") || lower.contains("dead-letter") || lower.contains("deadletter");
    }

    private int parseInt(String value) {
        if (value == null) return 0;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return 0; }
    }

    private long parseLong(String value) {
        if (value == null) return 0L;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return 0L; }
    }
}
