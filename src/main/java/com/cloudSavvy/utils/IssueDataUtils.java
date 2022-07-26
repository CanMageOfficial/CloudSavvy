package com.cloudSavvy.utils;

import com.cloudSavvy.aws.common.IssueSeverity;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.localization.TextConstants;
import com.cloudSavvy.reporting.ReportType;
import org.apache.commons.collections4.MapUtils;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IssueDataUtils {
    private static Map<IssueSeverity, Integer> buildIssueCountMap(final List<RegionAnalyzeResult> regionResults) {
        Map<IssueSeverity, Integer> issueSevCountMap = new HashMap<>();
        for (RegionAnalyzeResult result : regionResults) {
            for (IssueData issueData : result.getIssueDataList()) {
                IssueSeverity sev = issueData.getIssueCode().getIssueSeverity();
                Integer count = issueSevCountMap.getOrDefault(sev, 0);
                issueSevCountMap.put(sev, count + 1);
            }
        }
        return issueSevCountMap;
    }

    private static int getNewIssueCount(final List<RegionAnalyzeResult> regionResults) {
        int count = 0;
        for (RegionAnalyzeResult result : regionResults) {
            count += result.getNewIssues().size();
        }
        return count;
    }

    private static long countOfRegionsWithIssue(final List<RegionAnalyzeResult> regionResults) {
        return regionResults.stream()
                .filter(result -> !CollectionUtils.isNullOrEmpty(result.getIssueDataList())).count();
    }

    public static List<IssueSeverity> getSortedSeverities() {
        return Arrays.stream(IssueSeverity.values())
                .sorted(Comparator.comparingInt(IssueSeverity::getValue).reversed()).collect(Collectors.toList());
    }

    public static String buildIssueDataMessage(final List<RegionAnalyzeResult> regionResults, boolean isShort) {
        if (CollectionUtils.isNullOrEmpty(regionResults)) {
            return TextConstants.NO_ISSUE_FOUND;
        }

        Map<IssueSeverity, Integer> issueCountMap = IssueDataUtils.buildIssueCountMap(regionResults);
        int newIssueCount = IssueDataUtils.getNewIssueCount(regionResults);

        if (MapUtils.isEmpty(issueCountMap)) {
            return TextConstants.NO_ISSUE_FOUND;
        }

        List<IssueSeverity> sortedSeverities = IssueDataUtils.getSortedSeverities();
        StringBuilder message = new StringBuilder();

        if (newIssueCount > 0) {
            message.append(newIssueCount).append(" new issues found.");

            if (isShort) {
               return message.toString();
            }

            message.append(" Total ");
        }

        boolean firstIssue = true;
        for (IssueSeverity sev : sortedSeverities) {
            if (issueCountMap.containsKey(sev)) {
                if (!firstIssue) {
                    message.append(", ");
                }
                message.append(issueCountMap.get(sev)).append(" ").append(sev.toString());
                firstIssue = false;
            }
        }

        long regionsWithIssueCount = IssueDataUtils.countOfRegionsWithIssue(regionResults);
        message.append(" severity issues found in ").append(regionsWithIssueCount).append(" regions.");
        return message.toString();
    }

    public static String getS3ObjectLocation(ReportType reportType, String outputBucketFolder) {
        if (reportType == ReportType.INTERNAL_DATA_JSON) {
            return reportType.getFileName();
        }

        return outputBucketFolder + "/" + reportType.getFileName();
    }
}
