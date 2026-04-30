package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.aws.billing.DailyCharge;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.comparator.BillingDataPriceComparator;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.HtmlReportUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.billing.BillingData;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DailyChargesHtmlReport implements ReportBuilder {

    private static final String ENABLE_METRICS_LINK =
            "https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/monitor_estimated_charges_with_cloudwatch.html#turning_on_billing_metrics";
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public ReportResult generateReport(final GenerateReportInput input) {
        final List<RegionAnalyzeResult> results = input.getResults();
        Optional<RegionAnalyzeResult> eastResultOptional =
                results.stream().filter(result -> result.getRegion() == Region.US_EAST_1).findFirst();

        StringBuilder sb = new StringBuilder();
        HtmlReportUtils.appendResultHead(sb);

        sb.append("<h2>").append("Estimated Charges").append("</h2>");

        if (eastResultOptional.isEmpty()
                || CollectionUtils.isNullOrEmpty(eastResultOptional.get().getBillingDataList())) {
            sb.append("<p>").append("No charging data found.").append("</p>");
            sb.append(HtmlReportUtils.buildAnchorLink(ENABLE_METRICS_LINK, "Please enable metrics to see estimated charges."));
        } else {
            List<BillingData> billingDataList = eastResultOptional.get().getBillingDataList();
            buildChargesTable(billingDataList, sb);

            sb.append("<p>").append("These values are not final values.").append("</p>");
        }

        sb.append("<p>").append("Please refer to AWS billing page for final values: ");
        sb.append(HtmlReportUtils.buildAnchorLink("https://us-east-1.console.aws.amazon.com/billing/home#/",
                "AWS Billing Page")).append("</p>");
        HtmlReportUtils.appendTail(sb);

        return ReportResult.builder().report(sb.toString()).build();
    }

    @Override
    public ReportType getReportType() {
        return ReportType.DAILY_CHARGES_HTML;
    }

    private void buildChargesTable(List<BillingData> billingDataList, StringBuilder sb) {
        sb.append("<div style=\"overflow-x:auto;-webkit-overflow-scrolling:touch;\">");
        sb.append("<table style=\"width:auto;min-width:100%;white-space:nowrap;\">").append("<thead><tr>");
        sb.append(HtmlReportUtils.buildHeaderCell("Service Name"));

        List<BillingData> sortedBillingDataList =
                billingDataList.stream().sorted(new BillingDataPriceComparator()).toList();
        List<DailyCharge> longestData = sortedBillingDataList.stream().map(BillingData::getDailyCharges)
                .max(Comparator.comparingInt(List::size)).orElse(new ArrayList<>());
        Collections.reverse(longestData);
        for (DailyCharge dailyCharge : longestData) {
            sb.append(HtmlReportUtils.buildHeaderCell(TimeUtils.DAY_FORMATTER.format(Date.from(dailyCharge.getDate()))));
        }
        sb.append("</tr></thead><tbody>");
        for (BillingData billingData : sortedBillingDataList) {
            sb.append("<tr>").append(HtmlReportUtils.buildColumnText(billingData.getServiceName()));
            for (int i = billingData.getDailyCharges().size() - 1; i >= 0; i--) {
                double chargeValue = billingData.getDailyCharges().get(i).getValue();
                sb.append(HtmlReportUtils.buildColumnText(decimalFormat.format(chargeValue)));
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
    }
}
