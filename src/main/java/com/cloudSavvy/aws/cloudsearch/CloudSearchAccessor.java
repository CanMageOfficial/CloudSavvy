package com.cloudSavvy.aws.cloudsearch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudsearch.CloudSearchClient;
import software.amazon.awssdk.services.cloudsearch.model.CloudSearchException;
import software.amazon.awssdk.services.cloudsearch.model.DescribeDomainsResponse;
import software.amazon.awssdk.services.cloudsearch.model.DomainStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CloudSearchAccessor {
    private CloudSearchClient cloudSearchClient;

    public List<DomainStatus> listDomainNames() {
        try {
            DescribeDomainsResponse domainNamesResponse = cloudSearchClient.describeDomains();
            return domainNamesResponse.domainStatusList().stream().filter(domainStatus -> !domainStatus.deleted())
                    .collect(Collectors.toList());
        } catch (CloudSearchException e) {
            // 401 means CloudSearch is not supported for this account (e.g. new domain creation disabled)
            if (e.statusCode() == 401) {
                log.warn("CloudSearch is not supported for this account: {}", e.getMessage());
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
