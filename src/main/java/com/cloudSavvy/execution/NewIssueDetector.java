package com.cloudSavvy.execution;

import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.internal.ExRegionRunResult;
import com.cloudSavvy.common.internal.ExecutionInternalData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.EnvironmentUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class NewIssueDetector {
    final ObjectMapper objectMapper;
    final S3Client s3Client;

    public void addNewIssues(ExecutionInput executionInput, List<RegionAnalyzeResult> regionResults) {

        ExecutionInternalData internalData = null;
        if (EnvironmentUtils.isRunningInLambda()) {
            // Download from S3
            try {
                GetObjectRequest objectRequest = GetObjectRequest.builder()
                        .bucket(executionInput.getOutputBucketName()).key(ReportType.INTERNAL_DATA_JSON.getFileName())
                        .build();
                ResponseBytes<GetObjectResponse> result = s3Client.getObjectAsBytes(objectRequest);

                if (result.asByteArray().length > 0) {
                    internalData = objectMapper.readValue(result.asByteArray(), ExecutionInternalData.class);
                }
            } catch (Exception e) {
                if (!(e instanceof NoSuchKeyException)) {
                    log.error("Could not get internal data file CloudSavvyData from S3", e);
                }
            }
        } else {
            Path outDir = executionInput.getOutputDirectoryPath();
            Path internalDataPath = outDir.resolve(ReportType.INTERNAL_DATA_JSON.getFileName());
            if (Files.exists(internalDataPath)) {
                try {
                    internalData = objectMapper.readValue(internalDataPath.toFile(), ExecutionInternalData.class);
                } catch (IOException e) {
                    log.error("Could not read internal data file CloudSavvyData", e);
                }
            }
        }
        addNewIssues(internalData, regionResults);
    }

    private void addNewIssues(ExecutionInternalData internalData, List<RegionAnalyzeResult> regionResults) {
        if (internalData == null) {
            return;
        }

        if (!CollectionUtils.isNullOrEmpty(internalData.getRegionRunResults())) {
            Map<String, Set<IssueData>> regionNameToExResultsMap = internalData.getRegionRunResults()
                    .stream().collect(Collectors.toMap(ExRegionRunResult::getRegionName,
                            p -> new HashSet<>(p.getDetectedIssues())));
            for (RegionAnalyzeResult analyzeResult : regionResults) {
                if (!CollectionUtils.isNullOrEmpty(analyzeResult.getIssueDataList())) {
                    String regionName = analyzeResult.getRegion().id();
                    if (regionNameToExResultsMap.containsKey(regionName)) {
                        Set<IssueData> exIssueDataSet = regionNameToExResultsMap.get(regionName);
                        for (IssueData issueData : analyzeResult.getIssueDataList()) {
                            if (!exIssueDataSet.contains(issueData)) {
                                analyzeResult.addNewIssue(issueData);
                            }
                        }
                    } else {
                        for (IssueData issueData : analyzeResult.getIssueDataList()) {
                            analyzeResult.addNewIssue(issueData);
                        }
                    }
                }
            }
        }
    }
}
