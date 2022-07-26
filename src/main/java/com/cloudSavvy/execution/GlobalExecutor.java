package com.cloudSavvy.execution;

import com.cloudSavvy.GlobalExceptionHandler;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.processor.ProcessReportResult;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

import java.time.Instant;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class GlobalExecutor {

    private final GlobalAnalyzer globalAnalyzer;
    private final ReportExecutor reportExecutor;
    private final NewIssueDetector newIssueDetector;

    public ExecutionResult execute(final ExecutionInput executionInput) {
        final Instant startTime = Instant.now();
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        List<Region> regions = globalAnalyzer.findRegionsToAnalyze(executionInput);
        List<RegionAnalyzeResult> regionResults = globalAnalyzer.analyzeRegions(regions);
        newIssueDetector.addNewIssues(executionInput, regionResults);
        List<ProcessReportResult> processReportResults =
                reportExecutor.processReports(executionInput, regions, regionResults);

        printDurationMessage(startTime);
        return ExecutionResult.builder().reportResults(processReportResults).build();
    }

    private void printDurationMessage(Instant startTime) {
        String durationMessage = TimeUtils.getDiffInPrettyFormat(startTime, Instant.now());
        log.info("Completed! Duration: {}", durationMessage);
    }
}
