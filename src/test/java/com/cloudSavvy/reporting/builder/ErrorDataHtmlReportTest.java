package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.common.ErrorData;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.execution.ExecutionInput;
import com.cloudSavvy.utils.S3UrlBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErrorDataHtmlReportTest {
    private static final String CONNECTION_FAILED = "Connection failed";
    private static final String CONNECTION_FAILED_2 = "Connection failed 2";
    private static final String NOT_SUPPORTED = "Not supported";

    @Test
    public void test_generateReport() {
        S3UrlBuilder s3UrlBuilder = Mockito.mock(S3UrlBuilder.class);
        ErrorDataHtmlReport htmlReport = new ErrorDataHtmlReport(s3UrlBuilder);
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.US_EAST_1);
        result.getErrorDataList().add(new ErrorData(AWSService.S3, CONNECTION_FAILED));
        result.getErrorDataList().add(new ErrorData(AWSService.S3, CONNECTION_FAILED_2));
        result.getErrorDataList().add(new ErrorData(AWSService.CloudFront, NOT_SUPPORTED));
        results.add(result);

        RegionAnalyzeResult result2 = new RegionAnalyzeResult(Region.US_EAST_2);
        result2.getErrorDataList().add(new ErrorData(AWSService.VPC, CONNECTION_FAILED));
        result2.getErrorDataList().add(new ErrorData(AWSService.DynamoDB, CONNECTION_FAILED_2));
        result2.getErrorDataList().add(new ErrorData(AWSService.Amazon_EventBridge, NOT_SUPPORTED));
        results.add(result2);

        GenerateReportInput generateReportInput = GenerateReportInput.builder().results(results)
                .analyzedRegions(List.of(Region.AF_SOUTH_1, Region.EU_WEST_2))
                .executionInput(ExecutionInput.builder().build()).build();
        ReportResult reportResult = htmlReport.generateReport(generateReportInput);
        assertNotNull(reportResult);
        Document doc = Jsoup.parse(reportResult.getReport());
        assertNotNull(doc.body());
        assertTrue(reportResult.getReport().contains(NOT_SUPPORTED));
    }
}
