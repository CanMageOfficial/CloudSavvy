package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.IssueDataUtils;

import java.util.List;

public class ShortMessageReport implements ReportBuilder {
    @Override
    public ReportType getReportType() {
        return ReportType.SHORT_MESSAGE;
    }

    @Override
    public ReportResult generateReport(GenerateReportInput input) {
        List<RegionAnalyzeResult> results = input.getResults();
        String issueMessage = IssueDataUtils.buildIssueDataMessage(results, true);
        return ReportResult.builder().report(issueMessage).build();
    }
}
