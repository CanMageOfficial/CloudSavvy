package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.localization.LocalizationReader;
import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.reporting.builder.ReportResult;
import com.cloudSavvy.utils.FileUtils;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FileReportProcessor implements ReportProcessor {

    private static final Set<ReportType> SUPPORTED_REPORT_TYPES = ImmutableSet.of(ReportType.ERROR_DATA_HTML,
            ReportType.FULL_ISSUE_DATA_HTML, ReportType.SERVICE_DATA_HTML, ReportType.DAILY_CHARGES_HTML);

    @Override
    public ProcessReportResult processReport(final @NonNull ProcessReportInput input) {
        if (input.reportTypeDataMap.isEmpty()) {
            throw new RuntimeException("No report is provided to FileReportProcessor");
        }

        Map<ReportType, String> fileNames = new ConcurrentHashMap<>();
        Path outputDir = input.getOutputDirectoryPath();
        ResourceBundle resourceBundle = LocalizationReader.getMessageTexts(Locale.US);

        Path resultsDir = outputDir.resolve(input.getOutputFolderName());
        try {
            if (!Files.exists(resultsDir)) {
                Files.createDirectories(resultsDir);
            }
        } catch (Exception exc) {
            log.error("Cannot create report date folder", exc);
            resultsDir = outputDir;
        }

        Path finalResultsDir = resultsDir;
        input.reportTypeDataMap.entrySet().parallelStream().forEach(entry -> {
            ReportType reportType = entry.getKey();
            if (!SUPPORTED_REPORT_TYPES.contains(reportType)) {
                return;
            }

            String fileName = reportType.getFileName();
            try {
                String filePath = finalResultsDir.resolve(fileName).toString();
                FileUtils.writeToFile(filePath, entry.getValue().getReport());

                if (reportType == ReportType.FULL_ISSUE_DATA_HTML || reportType == ReportType.SERVICE_DATA_HTML) {
                    log.info("{}: {}", resourceBundle.getString(reportType.name()), filePath);
                }
                fileNames.put(reportType, filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // This is internal data only used by system
        try {
            String filePath = outputDir.resolve(ReportType.INTERNAL_DATA_JSON.getFileName()).toString();
            ReportResult internalReport =
                    input.reportTypeDataMap.getOrDefault(ReportType.INTERNAL_DATA_JSON, null);
            if (internalReport != null && !StringUtils.isEmpty(internalReport.getReport())) {
                FileUtils.writeToFile(filePath, internalReport.getReport());
            }
        } catch (IOException e) {
            log.error("Cannot write internal application data", e);
        }

        return ProcessReportResult.builder().reportLocationType(ReportLocationType.FILE).locations(fileNames).build();
    }
}
