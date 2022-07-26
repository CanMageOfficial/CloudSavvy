package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.reporting.ReportType;

public interface ReportBuilder {
    ReportType getReportType();

    ReportResult generateReport(GenerateReportInput input);
}
