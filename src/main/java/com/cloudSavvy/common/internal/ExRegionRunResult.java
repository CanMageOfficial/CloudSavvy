package com.cloudSavvy.common.internal;

import com.cloudSavvy.common.IssueData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExRegionRunResult {
    private List<IssueData> detectedIssues;
    private String regionName;
}
