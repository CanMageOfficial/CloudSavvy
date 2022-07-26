package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.utils.EnvironmentUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class ReportProcessorFactory {
    private S3ReportProcessor s3ReportProcessor;
    private FileReportProcessor fileReportProcessor;

    private EmailReportProcessor emailReportProcessor;
    private SNSReportProcessor snsReportProcessor;

    public List<ReportProcessor> getReportProcessors() {
        if (EnvironmentUtils.isRunningInLambda()) {
            return Arrays.asList(s3ReportProcessor, emailReportProcessor, snsReportProcessor);
        }
        return Collections.singletonList(fileReportProcessor);
    }
}
