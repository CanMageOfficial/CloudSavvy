package com.cloudSavvy.aws.bedrock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.ListProvisionedModelThroughputsRequest;
import software.amazon.awssdk.services.bedrock.model.ListProvisionedModelThroughputsResponse;
import software.amazon.awssdk.services.bedrock.model.ProvisionedModelStatus;
import software.amazon.awssdk.services.bedrock.model.ProvisionedModelSummary;
import software.amazon.awssdk.services.bedrock.paginators.ListProvisionedModelThroughputsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class BedrockAccessor {

    private BedrockClient bedrockClient;

    public List<ProvisionedModelSummary> listInServiceProvisionedThroughputs() {
        ListProvisionedModelThroughputsIterable iterable =
                bedrockClient.listProvisionedModelThroughputsPaginator(
                        ListProvisionedModelThroughputsRequest.builder().build());
        List<ProvisionedModelSummary> results = new ArrayList<>();
        for (ListProvisionedModelThroughputsResponse response : iterable) {
            for (ProvisionedModelSummary summary : response.provisionedModelSummaries()) {
                if (summary.status() == ProvisionedModelStatus.IN_SERVICE) {
                    results.add(summary);
                }
            }
            if (results.size() > 1000) {
                break;
            }
        }
        return results;
    }
}
