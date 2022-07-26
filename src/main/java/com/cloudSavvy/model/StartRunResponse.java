package com.cloudSavvy.model;

import com.yworks.util.annotation.Obfuscation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Obfuscation()
public class StartRunResponse {
    private Map<String, String> s3ReportLocations;
    private List<String> emailReportReceivers;
}
