package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.internal.ExRegionRunResult;
import com.cloudSavvy.common.internal.ExecutionInternalData;
import com.cloudSavvy.reporting.ReportType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.CollectionUtils;

@AllArgsConstructor
@Slf4j
public class InternalDataJsonReport implements ReportBuilder {
    final ObjectMapper objectMapper;

    @Override
    public ReportResult generateReport(final GenerateReportInput input) {
        ExecutionInternalData internalData = new ExecutionInternalData();
        for (RegionAnalyzeResult analyzeResult : input.getResults()) {
            if (!CollectionUtils.isNullOrEmpty(analyzeResult.getIssueDataList())) {
                internalData.getRegionRunResults().add(ExRegionRunResult.builder()
                        .regionName(analyzeResult.getRegion().id())
                        .detectedIssues(analyzeResult.getIssueDataList()).build());
            }
        }

        String report = "";
        try {
            report = objectMapper.writeValueAsString(internalData);
        } catch (JsonProcessingException e) {
            log.error("Creating internal data failed", e);
        }

        return ReportResult.builder().report(report).build();
    }

    @Override
    public ReportType getReportType() {
        return ReportType.INTERNAL_DATA_JSON;
    }
}
