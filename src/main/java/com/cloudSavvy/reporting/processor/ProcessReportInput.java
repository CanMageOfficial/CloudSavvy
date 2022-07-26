package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.reporting.builder.ReportResult;
import com.cloudSavvy.reporting.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Map;

@Builder
@Getter
public class ProcessReportInput {
    Map<ReportType, ReportResult> reportTypeDataMap;
    Path outputDirectoryPath;
    String outputBucketName;
    String outputFolderName;
}
