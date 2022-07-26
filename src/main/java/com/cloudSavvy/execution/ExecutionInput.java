package com.cloudSavvy.execution;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;

import java.nio.file.Path;
import java.util.List;

@Builder
@Getter
public class ExecutionInput {
    // if user does not request specific region, all regions will be analyzed
    private List<Region> requestedRegions;
    private boolean reportAllResources;
    private String awsAccountId;
    private String stackPath;
    private String runningRegion;
    private String outputBucketName;
    private Path outputDirectoryPath;
}
