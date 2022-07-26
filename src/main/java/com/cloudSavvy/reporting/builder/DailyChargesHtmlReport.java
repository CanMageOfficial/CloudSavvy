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
        appendHead(sb);

        sb.append("<h3>").append("Estimated Charges").append("</h3>");

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

    private void appendHead(final StringBuilder sb) {
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\"/>");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=2.0, user-scalable=yes\" />");
        sb.append("<style>");
        sb.append("table, th, td{border: 1px black; border-collapse: collapse;border-style: dotted}");
        sb.append("th, td{padding: 7px}");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
    }

    private void buildChargesTable(List<BillingData> billingDataList, StringBuilder sb) {
        sb.append("<table>").append("<tr>");
        sb.append(HtmlReportUtils.buildColumnText("Service Name"));

        List<BillingData> sortedBillingDataList =
                billingDataList.stream().sorted(new BillingDataPriceComparator()).collect(Collectors.toList());
        List<DailyCharge> longestData = sortedBillingDataList.stream().map(BillingData::getDailyCharges)
                .max(Comparator.comparingInt(List::size)).orElse(new ArrayList<>());
        Collections.reverse(longestData);
        for (DailyCharge dailyCharge : longestData) {
            sb.append(HtmlReportUtils.buildColumnText(TimeUtils.DAY_FORMATTER.format(Date.from(dailyCharge.getDate()))));
        }
        sb.append("</tr>");
        for (BillingData billingData : sortedBillingDataList) {
            sb.append("<tr>").append(HtmlReportUtils.buildColumnText(billingData.getServiceName()));
            for (int i = billingData.getDailyCharges().size() - 1; i >= 0; i--) {
                double chargeValue = billingData.getDailyCharges().get(i).getValue();
                sb.append(HtmlReportUtils.buildColumnText(decimalFormat.format(chargeValue)));
            }
            sb.append("</tr>");
        }

        sb.append("</table>");
    }
}
