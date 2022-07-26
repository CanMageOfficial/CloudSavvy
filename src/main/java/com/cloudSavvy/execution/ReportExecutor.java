package com.cloudSavvy.execution;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.reporting.builder.GenerateReportInput;
import com.cloudSavvy.reporting.builder.ReportResult;
import com.cloudSavvy.reporting.builder.ReportBuilderFactory;
import com.cloudSavvy.reporting.processor.ProcessReportInput;
import com.cloudSavvy.reporting.processor.ProcessReportResult;
import com.cloudSavvy.reporting.processor.ReportProcessorFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
public class ReportExecutor {

    private final ReportProcessorFactory reportProcessorFactory;
    private final ReportBuilderFactory reportBuilderFactory;

    public List<ProcessReportResult> processReports(final ExecutionInput executionInput,
                                                            List<Region> regions,
                                                            List<RegionAnalyzeResult> regionResults) {
        GenerateReportInput.GenerateReportInputBuilder reportInputBuilder = GenerateReportInput.from(executionInput);
        GenerateReportInput reportInput = reportInputBuilder.analyzedRegions(regions).results(regionResults).build();
        ProcessReportInput processReportInput = getProcessReportInput(reportInput);
        return executeReportProcessors(processReportInput);
    }

    private List<ProcessReportResult> executeReportProcessors(final ProcessReportInput processReportInput) {
        List<ProcessReportResult> processLocations = Collections.synchronizedList(new ArrayList<>());
        reportProcessorFactory.getReportProcessors().parallelStream()
                .forEach(reportProcessor -> {
                    try {
                        ProcessReportResult location = reportProcessor.processReport(processReportInput);
                        processLocations.add(location);
                    } catch (Exception e) {
                        log.error("Processing report failed", e);
                    }
                });
        return processLocations;
    }

    private ProcessReportInput getProcessReportInput(final GenerateReportInput input) {
        Map<ReportType, ReportResult> reportDataMap = new ConcurrentHashMap<>();

        reportBuilderFactory.getReportBuilders().parallelStream().forEach(reportBuilder -> {
            ReportResult reportResult = reportBuilder.generateReport(input);
            if (reportResult != null) {
                reportDataMap.put(reportBuilder.getReportType(), reportResult);
            }
        });

        return ProcessReportInput.builder().reportTypeDataMap(reportDataMap)
                .outputFolderName(input.getOutputFolderName())
                .outputDirectoryPath(input.getExecutionInput().getOutputDirectoryPath())
                .outputBucketName(input.getExecutionInput().getOutputBucketName()).build();
    }
}
