package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.aws.billing.BillingData;
import com.cloudSavvy.aws.billing.DailyCharge;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.execution.ExecutionInput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DailyChargesHtmlReportTest {

    @Test
    public void test_generateReport() {
        final String service1 = "service 1";
        DailyChargesHtmlReport htmlReport = new DailyChargesHtmlReport();
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.US_EAST_1);

        BillingData billingData1 = new BillingData();
        billingData1.setServiceName(service1);
        billingData1.getDailyCharges().add(DailyCharge.builder().value(3.2)
                .date(Instant.now()).build());
        billingData1.getDailyCharges().add(DailyCharge.builder().value(3.4)
                .date(Instant.now().minus(1, ChronoUnit.DAYS)).build());
        result.getBillingDataList().add(billingData1);

        BillingData billingData2 = new BillingData();
        billingData2.setServiceName("service 2");
        billingData2.getDailyCharges().add(DailyCharge.builder().value(0.22)
                .date(Instant.now()).build());
        billingData2.getDailyCharges().add(DailyCharge.builder().value(1.23)
                .date(Instant.now().minus(1, ChronoUnit.DAYS)).build());
        result.getBillingDataList().add(billingData2);

        results.add(result);

        GenerateReportInput generateReportInput = GenerateReportInput.builder().results(results)
                .analyzedRegions(List.of(Region.US_EAST_1, Region.EU_WEST_2))
                .executionInput(ExecutionInput.builder().build()).build();
        ReportResult reportResult = htmlReport.generateReport(generateReportInput);
        assertNotNull(reportResult);
        Document doc = Jsoup.parse(reportResult.getReport());
        assertNotNull(doc.body());
        assertTrue(reportResult.getReport().contains(service1), reportResult.getReport());
    }

    @Test
    public void test_generateReport_EmptyBillingData() {
        DailyChargesHtmlReport htmlReport = new DailyChargesHtmlReport();
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.US_EAST_1);
        results.add(result);
        GenerateReportInput generateReportInput = GenerateReportInput.builder().results(results)
                .analyzedRegions(List.of(Region.US_EAST_1, Region.EU_WEST_2))
                .executionInput(ExecutionInput.builder().build()).build();
        ReportResult reportResult = htmlReport.generateReport(generateReportInput);
        assertNotNull(reportResult);
        Document doc = Jsoup.parse(reportResult.getReport());
        assertNotNull(doc.body());
    }
}
