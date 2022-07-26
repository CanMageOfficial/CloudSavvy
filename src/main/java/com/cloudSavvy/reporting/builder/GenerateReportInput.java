package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.execution.ExecutionInput;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import software.amazon.awssdk.regions.Region;

import java.util.List;

@Builder
@Getter
public class GenerateReportInput {
    @NonNull
    private List<RegionAnalyzeResult> results;
    @NonNull
    private List<Region> analyzedRegions;
    private String outputFolderName;
    @NonNull
    private ExecutionInput executionInput;

    public static GenerateReportInput.GenerateReportInputBuilder from(final ExecutionInput executionInput) {
        return GenerateReportInput.builder()
                .executionInput(executionInput)
                .outputFolderName(TimeUtils.getFileFormattedCurrentTime());
    }
}
