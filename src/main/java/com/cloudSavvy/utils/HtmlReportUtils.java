package com.cloudSavvy.utils;

import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.reporting.builder.GenerateReportInput;
import com.cloudSavvy.aws.common.EntityLinks;
import com.cloudSavvy.aws.common.EntityType;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.StringUtils;

public class HtmlReportUtils {
    public static final String REGION_HEADER_START = "<h3>";
    public static final String REGION_HEADER_END = "</h3>";
    public static final String SECTION_HEADER_START = "<h4>";
    public static final String SECTION_HEADER_END = "</h4>";

    public static void appendResultHead(final StringBuilder sb) {
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\"/>");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=2.0, user-scalable=yes\" />");
        sb.append("<style>");
        sb.append("h2{text-align: center;}");
        sb.append("h3{text-align: center;}");
        sb.append("h4{margin-bottom: 5px}");
        sb.append(".issueDetails{margin-left: 17px;margin-top: 5px; margin-bottom: 5px;}");
        sb.append(".shortLine{width:70%; margin-right:30%;}");
        sb.append(".sectionLine{border: 1px solid black;}");
        sb.append(".centerText{margin-bottom: 20px;text-align: center;}");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
    }

    public static void appendTail(final StringBuilder sb) {
        sb.append("</body>");
        sb.append("</html>");
    }

    public static String buildAnchorLink(final String link, final String text) {
        return String.format("<a href=\"%1$s\">%2$s</a>", link, text);
    }

    public static String buildResourceRow(Region region, EntityType entityType, String resourceId) {
        String link = EntityLinks.getEntityLink(region, entityType, resourceId);
        if (!StringUtils.isEmpty(link)) {
            return buildAnchorLink(link, resourceId);
        } else {
            return resourceId;
        }
    }

    public static String buildResourceRow(Region region, IssueData issueData) {
        String link;
        if (issueData.getEntityType().isContainer()) {
            link = EntityLinks.getEntityLink(region, issueData.getEntityType(),
                    issueData.getContainerName(), issueData.getEntityName());
        } else {
            link = EntityLinks.getEntityLink(region, issueData.getEntityType(), issueData.getEntityName());
        }

        if (!StringUtils.isEmpty(link)) {
            return buildAnchorLink(link, issueData.getEntityName());
        } else {
            return issueData.getEntityName();
        }
    }

    public static String buildRegionText(Region region) {
        return "<hr class=\"shortLine\" />".concat(REGION_HEADER_START).concat("Region: ")
                .concat(region.id()).concat(REGION_HEADER_END);
    }

    public static String buildColumnText(String text) {
        return "<td>".concat(text).concat("</td>");
    }

    public static String buildRunSettingsText(GenerateReportInput input, String allResourcesLink,
                                              String estimatedChargesLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("<hr class=\"sectionLine\" />");
        sb.append("<h2>Analysis Settings</h2>");
        sb.append("<p><b>Analyzed Regions:</b> ").append(input.getAnalyzedRegions()).append("</p>");
        if (!StringUtils.isEmpty(input.getExecutionInput().getAwsAccountId())) {
            sb.append("<p><b>AWS Account Id:</b> ").append(input.getExecutionInput().getAwsAccountId()).append("</p>");
        }

        sb.append("<p><b>Analysis Time:</b> ").append(TimeUtils.getUserFormattedCurrentTime()).append("</p>");

        if (!StringUtils.isEmpty(allResourcesLink)) {
            sb.append("<p>").append(buildAnchorLink(allResourcesLink, "Analyzed Resources")).append("</p>");
        }

        if (!StringUtils.isEmpty(estimatedChargesLink)) {
            sb.append("<p>").append(buildAnchorLink(estimatedChargesLink, "Estimated Charges For AWS Services"))
                    .append("</p>");
        }

        sb.append("<hr class=\"sectionLine\" />");
        return sb.toString();
    }
}
