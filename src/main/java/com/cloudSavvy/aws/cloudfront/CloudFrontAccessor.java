package com.cloudSavvy.aws.cloudfront;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsResponse;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class CloudFrontAccessor {
    private CloudFrontClient cloudFrontClient;

    public List<DistributionSummary> listDistributions() {
        String token = null;
        List<DistributionSummary> summaries = new ArrayList<>();
        do {
            ListDistributionsRequest request = ListDistributionsRequest.builder().marker(token).build();
            ListDistributionsResponse distributionsResponse = cloudFrontClient.listDistributions(request);
            summaries.addAll(distributionsResponse.distributionList().items());
            token = distributionsResponse.distributionList().nextMarker();
        } while (token != null && summaries.size() < 1000);
        log.debug("CloudFront Distribution size: {}", summaries.size());
        return summaries;
    }

}
