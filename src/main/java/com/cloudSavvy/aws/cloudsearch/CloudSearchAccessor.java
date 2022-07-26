package com.cloudSavvy.aws.cloudsearch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudsearch.CloudSearchClient;
import software.amazon.awssdk.services.cloudsearch.model.DescribeDomainsResponse;
import software.amazon.awssdk.services.cloudsearch.model.DomainStatus;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CloudSearchAccessor {
    private CloudSearchClient cloudSearchClient;

    public List<DomainStatus> listDomainNames() {
        DescribeDomainsResponse domainNamesResponse = cloudSearchClient.describeDomains();
        return domainNamesResponse.domainStatusList().stream().filter(domainStatus -> !domainStatus.deleted())
                .collect(Collectors.toList());
    }
}
