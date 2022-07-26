package com.cloudSavvy.aws.sagemaker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.EndpointStatus;
import software.amazon.awssdk.services.sagemaker.model.EndpointSummary;
import software.amazon.awssdk.services.sagemaker.model.NotebookInstanceStatus;
import software.amazon.awssdk.services.sagemaker.model.NotebookInstanceSummary;
import software.amazon.awssdk.services.sagemaker.paginators.ListEndpointsIterable;
import software.amazon.awssdk.services.sagemaker.paginators.ListNotebookInstancesIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class SageMakerAccessor {

    private SageMakerClient sageMakerClient;

    // https://aws.amazon.com/premiumsupport/knowledge-center/sagemaker-notebook-pending-fails/
    public List<NotebookInstanceSummary> listNotebookInstances() {
        ListNotebookInstancesIterable notebookInstancesResponses = sageMakerClient.listNotebookInstancesPaginator();
        List<NotebookInstanceSummary> instances = new ArrayList<>();
        for (NotebookInstanceSummary instance : notebookInstancesResponses.notebookInstances()) {
            if (instance.notebookInstanceStatus() != NotebookInstanceStatus.DELETING
                    && instance.notebookInstanceStatus() != NotebookInstanceStatus.PENDING) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public List<EndpointSummary> listEndpoints() {
        ListEndpointsIterable endpointsResponses = sageMakerClient.listEndpointsPaginator();
        List<EndpointSummary> endpointSummaries = new ArrayList<>();
        for (EndpointSummary endpoint : endpointsResponses.endpoints()) {
            if (endpoint.endpointStatus() != EndpointStatus.DELETING
                    && endpoint.endpointStatus() != EndpointStatus.CREATING) {
                endpointSummaries.add(endpoint);
            }
        }
        return endpointSummaries;
    }
}
