package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.ErrorData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.HtmlReportUtils;
import com.cloudSavvy.utils.S3UrlBuilder;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ErrorDataHtmlReport implements ReportBuilder {

    private final S3UrlBuilder s3UrlBuilder;

    @Override
    public ReportResult generateReport(final GenerateReportInput input) {
        Map<Region, List<ErrorData>> regionToDataMap = getRegionErrorDataMap(input.getResults());

        if (regionToDataMap.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        HtmlReportUtils.appendResultHead(sb);

        for (Map.Entry<Region, List<ErrorData>> entry : regionToDataMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            Region region = entry.getKey();
            sb.append(HtmlReportUtils.buildRegionText(region));
            int index = 1;
            for (ErrorData errorData : entry.getValue()) {
                sb.append("<h3>").append(index++).append(". ").append(errorData.getAwsService()).append("</h3>");
                sb.append("<table id=\"").append(errorData.getAwsService()).append("_").append(region).append("+\">");
                sb.append("<tr>").append(errorData.getErrorMessage()).append("</tr>");
                sb.append("</table>");
            }
        }
        String analyzedResourcesLink = s3UrlBuilder.buildPrivateS3Url(input, ReportType.SERVICE_DATA_HTML);
        String estimatedChargesLink = s3UrlBuilder.buildPrivateS3Url(input, ReportType.DAILY_CHARGES_HTML);
        sb.append(HtmlReportUtils.buildRunSettingsText(input, analyzedResourcesLink, estimatedChargesLink));
        HtmlReportUtils.appendTail(sb);
        return ReportResult.builder().report(sb.toString()).build();
    }

    private Map<Region, List<ErrorData>> getRegionErrorDataMap(final List<RegionAnalyzeResult> regionAnalyzeResults) {
        Map<Region, List<ErrorData>> regionToDataMap = new HashMap<>();
        for (RegionAnalyzeResult result : regionAnalyzeResults) {
            List<ErrorData> errorDataList = regionToDataMap.getOrDefault(result.getRegion(), new ArrayList<>());
            if (!CollectionUtils.isNullOrEmpty(result.getErrorDataList())) {
                errorDataList.addAll(result.getErrorDataList());
                regionToDataMap.put(result.getRegion(), errorDataList);
            }
        }
        return regionToDataMap;
    }

    @Override
    public ReportType getReportType() {
        return ReportType.ERROR_DATA_HTML;
    }
}
