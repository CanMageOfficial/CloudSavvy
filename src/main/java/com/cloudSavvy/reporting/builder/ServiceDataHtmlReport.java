package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.comparator.ResourceDateComparator;
import com.cloudSavvy.utils.HtmlReportUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.EntityLinks;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.reporting.ReportType;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceDataHtmlReport implements ReportBuilder {
    @Override
    public ReportResult generateReport(GenerateReportInput input) {
        Map<Region, List<ServiceData>> regionToDataMap = getRegionServiceDataMap(input.getResults());
        StringBuilder sb = new StringBuilder();

        if (regionToDataMap.isEmpty()) {
            sb.append("<p>").append("No resource detected.").append("</p>");
            sb.append(HtmlReportUtils.buildRunSettingsText(input, null, null));
            return ReportResult.builder().report(sb.toString()).build();
        }

        HtmlReportUtils.appendResultHead(sb);

        sb.append("<p class=\"centerText\">").append("These are the resources analyzed. ")
                .append("Only most recently updated 5 resources are listed. ")
                .append("Time near resources is the last update time. ").append("</p>");
        sb.append("<hr class=\"sectionLine\" />");
        sb.append("<h2>Resources</h2>");

        int numOfResourceToReport = input.getExecutionInput().isReportAllResources() ? 1000 : 5;
        buildResourcesView(regionToDataMap, numOfResourceToReport, sb);

        sb.append(HtmlReportUtils.buildRunSettingsText(input, null, null));
        HtmlReportUtils.appendTail(sb);
        return ReportResult.builder().report(sb.toString()).build();
    }

    private Map<Region, List<ServiceData>> getRegionServiceDataMap(List<RegionAnalyzeResult> regionAnalyzeResults) {
        Map<Region, List<ServiceData>> regionToDataMap = new HashMap<>();
        for (RegionAnalyzeResult result : regionAnalyzeResults) {
            List<ServiceData> serviceDataList = regionToDataMap.getOrDefault(result.getRegion(), new ArrayList<>());
            serviceDataList.addAll(result.getServiceDataMap().values());

            if (!CollectionUtils.isNullOrEmpty(serviceDataList)) {
                regionToDataMap.put(result.getRegion(), serviceDataList);
            }
        }
        return regionToDataMap;
    }

    private String buildServiceRow(Region region, EntityType entityType, int countOfResources, int index) {
        String link = EntityLinks.getServiceLink(region, entityType);
        StringBuilder sb = new StringBuilder();
        sb.append(HtmlReportUtils.SECTION_HEADER_START).append(index).append(". ");
        String title = String.format("%s (%s resources)", entityType, countOfResources);
        if (!StringUtils.isEmpty(link) && countOfResources > 0) {
            sb.append(HtmlReportUtils.buildAnchorLink(link, title));
        } else {
            sb.append(title);
        }
        sb.append(HtmlReportUtils.SECTION_HEADER_END);
        return sb.toString();
    }

    private void buildResourcesView(Map<Region, List<ServiceData>> regionToDataMap, int numOfResourceToReport,
                                    StringBuilder sb) {
        for (Map.Entry<Region, List<ServiceData>> entry : regionToDataMap.entrySet()) {
            if (CollectionUtils.isNullOrEmpty(entry.getValue())) {
                continue;
            }

            Region region = entry.getKey();
            sb.append(HtmlReportUtils.buildRegionText(region));
            int index = 1;
            for (ServiceData serviceData : entry.getValue()) {
                sb.append(buildServiceRow(region, serviceData.getEntityType(),
                        serviceData.getResources().size(), index++));
                sb.append("<ul>");

                List<ResourceMetadata> sortedResources =
                        serviceData.getResources().stream().sorted(new ResourceDateComparator()).collect(Collectors.toList());
                for (int i = 0; i < Math.min(numOfResourceToReport, sortedResources.size()); i++) {
                    String resourceId = sortedResources.get(i).getResourceId();
                    sb.append("<li>");
                    sb.append(HtmlReportUtils.buildResourceRow(region, serviceData.getEntityType(), resourceId));

                    if (sortedResources.get(i).getActivityDate() != null) {
                        String formattedActivityDate =
                                TimeUtils.getUserFormattedTime(sortedResources.get(i).getActivityDate());
                        sb.append("   (").append(formattedActivityDate).append(")");
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
        }
    }

    @Override
    public ReportType getReportType() {
        return ReportType.SERVICE_DATA_HTML;
    }
}
