package com.cloudSavvy.reporting.builder;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class ReportResult {
    @NonNull private String report;
}
