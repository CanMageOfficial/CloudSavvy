package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class ProcessReportResult {
    private ReportLocationType reportLocationType;
    private Map<ReportType, String> locations;
    private List<String> emailAddresses;
    private String snsTopicArn;
}
