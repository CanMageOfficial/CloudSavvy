package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.comparator.IssueCodeDataListComparator;
import com.cloudSavvy.localization.LocalizationReader;
import com.cloudSavvy.utils.S3UrlBuilder;
import com.cloudSavvy.aws.common.EntityLinks;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.comparator.IssueCodeComparator;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.HtmlReportUtils;
import com.cloudSavvy.utils.IssueDataUtils;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@AllArgsConstructor
public class IssueDataHtmlReport implements ReportBuilder {
    private final ReportType reportType;
    private final S3UrlBuilder s3UrlBuilder;

    private static String detectedIssueS3Location;

    private final Comparator<IssueCode> issueCodeComparator = new IssueCodeComparator();

    @Override
    public ReportResult generateReport(final GenerateReportInput input) {
        int numOfResourceToReport = input.getExecutionInput().isReportAllResources()
                || reportType == ReportType.FULL_ISSUE_DATA_HTML ? 50 : 5;
        return generateReport(input, numOfResourceToReport);
    }

    public ReportResult generateReport(final GenerateReportInput input, final int numOfResourceToReport) {
        final Map<Region, SortedMap<IssueCode, List<IssueData>>> regionToDataMap = getRegionIssueDataMap(input.getResults());

        StringBuilder sb = new StringBuilder();
        HtmlReportUtils.appendResultHead(sb);

        String issueMessage = IssueDataUtils.buildIssueDataMessage(input.getResults(), false);
        int analyzedRulesCount = IssueCode.values().length;
        sb.append("<p class=\"centerText\">").append(issueMessage)
                .append(" ").append(analyzedRulesCount).append(" rules are analyzed on ")
                .append(input.getAnalyzedRegions().size()).append(" regions.").append("</p>");
        sb.append("<hr class=\"sectionLine\" />");
        sb.append("<h2>Detected Issues</h2>");

        buildIssuesData(input, numOfResourceToReport, regionToDataMap, sb);

        String analyzedResourcesLink = s3UrlBuilder.buildPresignedS3Url(input, ReportType.SERVICE_DATA_HTML);
        String estimatedChargesLink = s3UrlBuilder.buildPresignedS3Url(input, ReportType.DAILY_CHARGES_HTML);
        sb.append(HtmlReportUtils.buildRunSettingsText(input, analyzedResourcesLink, estimatedChargesLink));
        if (!StringUtils.isEmpty(input.getExecutionInput().getStackPath())) {
            sb.append("<p>").append("You are getting this email because of the resources created by this ")
                    .append(HtmlReportUtils.buildAnchorLink(input.getExecutionInput().getStackPath(), "stack"))
                    .append("</p>");
        }
        HtmlReportUtils.appendTail(sb);
        return ReportResult.builder().report(sb.toString()).build();
    }

    private String buildIssueRow(Map.Entry<IssueCode, List<IssueData>> issueDataEntry) {
        return String.format("%s (%s resources)", issueDataEntry.getKey(), issueDataEntry.getValue().size());
    }

    private Map<Region, SortedMap<IssueCode, List<IssueData>>> getRegionIssueDataMap(
            final List<RegionAnalyzeResult> regionResults) {
        Map<Region, SortedMap<IssueCode, List<IssueData>>> regionToDataMap = new HashMap<>();
        for (RegionAnalyzeResult result : regionResults) {
            SortedMap<IssueCode, List<IssueData>> issueDataMap = regionToDataMap.getOrDefault(result.getRegion(),
                    new TreeMap<>(issueCodeComparator));
            for (IssueData issueData : result.getIssueDataList()) {
                List<IssueData> issueDataList = issueDataMap.getOrDefault(issueData.getIssueCode(), new ArrayList<>());
                issueDataList.add(issueData);
                issueDataMap.put(issueData.getIssueCode(), issueDataList);
            }
            if (!issueDataMap.isEmpty()) {
                regionToDataMap.put(result.getRegion(), issueDataMap);
            }
        }
        return regionToDataMap;
    }

    @Override
    public ReportType getReportType() {
        return reportType;
    }

    private String getDetectedIssueS3Location(GenerateReportInput input) {
        if (detectedIssueS3Location == null) {
            String objectKey = IssueDataUtils.getS3ObjectLocation(ReportType.FULL_ISSUE_DATA_HTML,
                    input.getOutputFolderName());
            detectedIssueS3Location =
                    EntityLinks.getS3ObjectPrefixLink(input.getExecutionInput().getOutputBucketName(),
                            input.getExecutionInput().getRunningRegion(), objectKey);
        }
        return detectedIssueS3Location;
    }

    private void buildIssuesData(final GenerateReportInput input, final int numOfResourceToReport,
                                 final Map<Region, SortedMap<IssueCode, List<IssueData>>> regionToDataMap,
                                 final StringBuilder sb) {
        final List<Map.Entry<Region, SortedMap<IssueCode, List<IssueData>>>> sortedDataList = regionToDataMap.entrySet()
                .stream().sorted(new IssueCodeDataListComparator()).collect(Collectors.toList());

        final Map<Object, Set<IssueData>> regionToNewIssueMap = input.getResults().stream()
                .collect(Collectors.toMap(RegionAnalyzeResult::getRegion, RegionAnalyzeResult::getNewIssues));

        List<IssueCode> issueCodes = List.of(IssueCode.values());
        Map<IssueCode, String> descriptionsMap = LocalizationReader.getIssueCodeShortDesc(Locale.US, issueCodes);
        Map<IssueCode, String> issueCodeLinksMap = LocalizationReader.getIssueCodeLinks(Locale.US, issueCodes);
        Map<IssueCode, String> issueCodeLinkTitles = LocalizationReader.getIssueCodeLinkTitles(Locale.US, issueCodes);

        for (Map.Entry<Region, SortedMap<IssueCode, List<IssueData>>> entry : sortedDataList) {
            if (entry.getValue().entrySet().isEmpty()) {
                continue;
            }

            Region region = entry.getKey();
            sb.append(HtmlReportUtils.buildRegionText(region));
            int index = 1;
            for (Map.Entry<IssueCode, List<IssueData>> issueDataEntry : entry.getValue().entrySet()) {
                IssueCode issueCode = issueDataEntry.getKey();
                sb.append(HtmlReportUtils.SECTION_HEADER_START).append(index++).append(". ")
                        .append(buildIssueRow(issueDataEntry)).append(HtmlReportUtils.SECTION_HEADER_END);
                if (descriptionsMap.containsKey(issueCode)) {
                    sb.append("<p class=\"issueDetails\">").append(descriptionsMap.get(issueCode)).append("</p>");
                }
                if (issueCodeLinksMap.containsKey(issueCode)) {
                    String issueCodeLink = HtmlReportUtils.buildAnchorLink(issueCodeLinksMap.get(issueCode),
                            issueCodeLinkTitles.get(issueCode));
                    sb.append("<p class=\"issueDetails\">Details: ").append(issueCodeLink).append("</p>");
                }
                sb.append("<p class=\"issueDetails\">").append("Cost Impact: ")
                        .append(issueCode.getIssueSeverity()).append("</p>");
                sb.append("<p class=\"issueDetails\">").append("Resources:").append("</p>");

                sb.append("<ul>");

                addResources(region, issueDataEntry, regionToNewIssueMap, numOfResourceToReport, sb);

                addMoreLink(numOfResourceToReport, issueDataEntry, input, sb);
                sb.append("</ul>");
            }
        }
    }

    private void addResources(Region region, Map.Entry<IssueCode, List<IssueData>> issueDataEntry,
                              Map<Object, Set<IssueData>> regionToNewIssueMap,
                              int numOfResourceToReport, StringBuilder sb) {

        Set<IssueData> newIssues = regionToNewIssueMap.getOrDefault(region, new HashSet<>());
        int count = 0;
        for (IssueData issueData : issueDataEntry.getValue()) {
            if (newIssues.contains(issueData)) {
                sb.append("<li>").append(HtmlReportUtils.buildResourceRow(region, issueData))
                        .append("<span style=\"color: red\"> NEW</span>").append("</li>");
                count++;
            }
        }

        int index = 0;
        while (index < issueDataEntry.getValue().size() &&
                count < Math.min(numOfResourceToReport, issueDataEntry.getValue().size())) {
            IssueData issueData = issueDataEntry.getValue().get(index);
            if (!newIssues.contains(issueData)) {
                sb.append("<li>").append(HtmlReportUtils.buildResourceRow(region, issueData)).append("</li>");
                count++;
            }
            index++;
        }
    }

    private void addMoreLink(int numOfResourcesReported, Map.Entry<IssueCode, List<IssueData>> issueDataEntry,
                             GenerateReportInput input, StringBuilder sb) {
        if (numOfResourcesReported < issueDataEntry.getValue().size()
                && !StringUtils.isEmpty(input.getOutputFolderName())) {
            String s3Location = getDetectedIssueS3Location(input);
            sb.append("<li>").append(HtmlReportUtils.buildAnchorLink(s3Location, "more..."))
                    .append("</li>");
        }
    }
}
