package com.cloudSavvy.execution;

import com.cloudSavvy.GlobalExceptionHandler;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.reporting.processor.ProcessReportResult;
import com.cloudSavvy.utils.IssueDataUtils;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class GlobalExecutor {

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";
    private static final String ORANGE = "\033[38;5;214m";
    private static final String GREEN  = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String LINE   = DIM + "─".repeat(62) + RESET;

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

        printSummary(startTime, regionResults, processReportResults);
        return ExecutionResult.builder().reportResults(processReportResults).build();
    }

    private void printSummary(Instant startTime, List<RegionAnalyzeResult> regionResults,
                              List<ProcessReportResult> processReportResults) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(nl);
        sb.append(LINE).append(nl);
        sb.append("  ").append(BOLD).append(ORANGE).append("CloudSavvy").append(RESET)
          .append("  Analysis Complete").append(nl);
        sb.append(LINE).append(nl);

        boolean hasIssues = regionResults.stream().anyMatch(r -> !CollectionUtils.isNullOrEmpty(r.getIssueDataList()));
        String issueColor = hasIssues ? YELLOW : GREEN;
        String issueMessage = IssueDataUtils.buildIssueDataMessage(regionResults, false);
        sb.append(nl).append("  ").append(issueColor).append(issueMessage).append(RESET).append(nl);

        for (ProcessReportResult result : processReportResults) {
            if (result.getReportLocationType() != ReportLocationType.FILE
                    || result.getLocations() == null) {
                continue;
            }
            Map<ReportType, String> locations = result.getLocations();
            appendFileLocation(sb, nl, "Detected Issues    ", locations.get(ReportType.FULL_ISSUE_DATA_HTML));
            appendFileLocation(sb, nl, "Analyzed Resources ", locations.get(ReportType.SERVICE_DATA_HTML));
            appendFileLocation(sb, nl, "Estimated Charges  ", locations.get(ReportType.DAILY_CHARGES_HTML));
        }

        String duration = TimeUtils.getDiffInPrettyFormat(startTime, Instant.now());
        sb.append(nl).append("  ").append(DIM).append("Duration: ").append(duration).append(RESET).append(nl);
        sb.append(LINE).append(nl);

        System.out.println(sb);
    }

    private void appendFileLocation(StringBuilder sb, String nl, String label, String path) {
        if (path == null) {
            return;
        }
        sb.append(nl).append("  ").append(BOLD).append(label).append(RESET).append(nl);
        sb.append("  ").append(DIM).append(path).append(RESET).append(nl);
    }
}
