package com.cloudSavvy.aws.sqs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.paginators.ListQueuesIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class SqsAccessor {

    private SqsClient sqsClient;

    public List<String> listQueueUrls() {
        ListQueuesIterable iterable = sqsClient.listQueuesPaginator();
        List<String> urls = new ArrayList<>();
        for (ListQueuesResponse response : iterable) {
            urls.addAll(response.queueUrls());
            if (urls.size() > 1000) {
                break;
            }
        }
        return urls;
    }

    public Map<QueueAttributeName, String> getQueueAttributes(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                                QueueAttributeName.QUEUE_ARN,
                                QueueAttributeName.CREATED_TIMESTAMP)
                        .build());
        return response.attributes();
    }
}
