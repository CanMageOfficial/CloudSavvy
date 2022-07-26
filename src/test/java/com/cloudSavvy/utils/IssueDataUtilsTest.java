package com.cloudSavvy.utils;

import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.IssueSeverity;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.localization.TextConstants;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IssueDataUtilsTest {

    @Test
    public void test_buildIssueDataMessage_noIssue() {
        List<RegionAnalyzeResult> results = new ArrayList<>();
        String message = IssueDataUtils.buildIssueDataMessage(results, false);
        assertEquals(TextConstants.NO_ISSUE_FOUND, message);
    }

    @Test
    public void test_buildIssueDataMessage_allIssues() {
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.AF_SOUTH_1);
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.HIGH)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.LOW)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.HIGH)));
        results.add(result);
        String message = IssueDataUtils.buildIssueDataMessage(results, false);
        assertEquals("2 High, 2 Medium, 1 Low severity issues found in 1 regions.", message);
    }

    @Test
    public void test_buildIssueDataMessage_highAndLowIssues() {
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.AF_SOUTH_1);
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.LOW)));
        results.add(result);
        String message = IssueDataUtils.buildIssueDataMessage(results, false);
        assertEquals("3 Medium, 1 Low severity issues found in 1 regions.", message);
    }

    @Test
    public void test_buildIssueDataMessage_mediumIssues() {
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.AF_SOUTH_1);
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        result.getIssueDataList().add(new IssueData(EntityType.AppStream_Fleet, "test",
                getIssue(IssueSeverity.MEDIUM)));
        results.add(result);
        String message = IssueDataUtils.buildIssueDataMessage(results, false);
        assertEquals("3 Medium severity issues found in 1 regions.", message);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private IssueCode getIssue(IssueSeverity sev) {
        return Arrays.stream(IssueCode.values()).filter(issue -> issue.getIssueSeverity() == sev)
                .findAny().get();
    }
}
