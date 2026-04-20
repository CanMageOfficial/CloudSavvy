package com.cloudSavvy.aws.sns;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.GetTopicAttributesRequest;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sns.paginators.ListTopicsIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class SnsAccessor {

    private SnsClient snsClient;

    public List<Topic> listTopics() {
        ListTopicsIterable iterable = snsClient.listTopicsPaginator();
        List<Topic> topics = new ArrayList<>();
        for (ListTopicsResponse response : iterable) {
            topics.addAll(response.topics());
            if (topics.size() > 1000) {
                break;
            }
        }
        return topics;
    }

    public Map<String, String> getTopicAttributes(String topicArn) {
        return snsClient.getTopicAttributes(GetTopicAttributesRequest.builder()
                .topicArn(topicArn).build()).attributes();
    }
}
