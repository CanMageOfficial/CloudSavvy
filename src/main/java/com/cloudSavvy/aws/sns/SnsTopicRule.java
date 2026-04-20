package com.cloudSavvy.aws.sns;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class SnsTopicRule implements AnalyzerRule {

    private SnsAccessor snsAccessor;

    private final EntityType entityType = EntityType.SNS_TOPIC;

    @Override
    public AWSService getAWSService() {
        return AWSService.SNS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Topic> topics = snsAccessor.listTopics();
        if (CollectionUtils.isNullOrEmpty(topics)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, topics.stream()
                .map(t -> new ResourceMetadata(t.topicArn(), null))
                .collect(Collectors.toList())));

        for (Topic topic : topics) {
            try {
                Map<String, String> attributes = snsAccessor.getTopicAttributes(topic.topicArn());
                String confirmed = attributes.getOrDefault("SubscriptionsConfirmed", "0");
                String pending = attributes.getOrDefault("SubscriptionsPending", "0");
                if (Integer.parseInt(confirmed) == 0 && Integer.parseInt(pending) == 0) {
                    ruleResult.addIssueData(new IssueData(entityType, topic.topicArn(),
                            IssueCode.SNS_TOPIC_HAS_NO_SUBSCRIPTIONS));
                }
            } catch (Exception e) {
                log.warn("Failed to get attributes for SNS topic {}: {}", topic.topicArn(), e.getMessage());
            }
        }

        return ruleResult;
    }
}
