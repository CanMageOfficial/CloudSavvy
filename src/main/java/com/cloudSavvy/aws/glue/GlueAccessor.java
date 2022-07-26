package com.cloudSavvy.aws.glue;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.BatchGetDevEndpointsRequest;
import software.amazon.awssdk.services.glue.model.BatchGetDevEndpointsResponse;
import software.amazon.awssdk.services.glue.model.DevEndpoint;
import software.amazon.awssdk.services.glue.model.ListDevEndpointsRequest;
import software.amazon.awssdk.services.glue.model.ListDevEndpointsResponse;
import software.amazon.awssdk.services.glue.paginators.ListDevEndpointsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class GlueAccessor {
    private GlueClient glueClient;

    public List<String> listDevEndPoints() {
        List<String> endpointNames = new ArrayList<>();

        ListDevEndpointsRequest request = ListDevEndpointsRequest.builder().build();
        ListDevEndpointsIterable endpointsIterable = glueClient.listDevEndpointsPaginator(request);
        for (ListDevEndpointsResponse endpointsResponse : endpointsIterable) {
            endpointNames.addAll(endpointsResponse.devEndpointNames());
        }
        return endpointNames;
    }

    public List<DevEndpoint> getDevEndPoints(final List<String> endpointNames) {
        BatchGetDevEndpointsRequest request = BatchGetDevEndpointsRequest.builder()
                .devEndpointNames(endpointNames).build();
        BatchGetDevEndpointsResponse endpointResponse = glueClient.batchGetDevEndpoints(request);
        return endpointResponse.devEndpoints();
    }
}
