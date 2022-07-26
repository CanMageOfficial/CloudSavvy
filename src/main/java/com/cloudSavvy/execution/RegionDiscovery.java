package com.cloudSavvy.execution;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsResponse;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RegionDiscovery {
    private Ec2Client ec2Client;

    public List<Region> getRegions() {
        DescribeRegionsResponse regionsResponse = ec2Client.describeRegions();
        return regionsResponse.regions().stream().map(region -> Region.of(region.regionName())).collect(Collectors.toList());
    }
}
