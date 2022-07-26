package com.cloudSavvy.execution;

import com.cloudSavvy.reporting.processor.ProcessReportResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ExecutionResult {
    private List<ProcessReportResult> reportResults;
}
