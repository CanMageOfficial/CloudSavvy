package com.cloudSavvy.reporting.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ReportBuilderFactory {
    private List<ReportBuilder> reportBuilders;
}
