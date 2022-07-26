package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.IssueDataUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;

@Slf4j
public class ShortMessageReport implements ReportBuilder {
    @Override
    public ReportType getReportType() {
        return ReportType.SHORT_MESSAGE;
    }

    @Override
    public ReportResult generateReport(GenerateReportInput input) {
        List<RegionAnalyzeResult> results = input.getResults();
        String issueMessage = IssueDataUtils.buildIssueDataMessage(results, true);
        if (!StringUtils.isEmpty(issueMessage)) {
            log.info(issueMessage);
        }

        return ReportResult.builder().report(issueMessage).build();
    }
}
