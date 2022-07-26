package com.cloudSavvy.reporting.builder;

import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
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

public class ServiceDataHtmlReportTest {

    @Test
    public void test_generateReport() {
        final String resource1 = "resource 1";
        ServiceDataHtmlReport htmlReport = new ServiceDataHtmlReport();
        List<RegionAnalyzeResult> results = new ArrayList<>();
        RegionAnalyzeResult result = new RegionAnalyzeResult(Region.US_EAST_1);

        List<ResourceMetadata> resourceMetadataList1 = new ArrayList<>();
        resourceMetadataList1
                .add(new ResourceMetadata("2", Instant.now()));
        resourceMetadataList1
                .add(new ResourceMetadata("3", Instant.now().plus(1, ChronoUnit.DAYS)));
        resourceMetadataList1
                .add(new ResourceMetadata("1", Instant.now().minus(1, ChronoUnit.DAYS)));
        ServiceData serviceData1 = new ServiceData(EntityType.APIGateway_API, resourceMetadataList1);
        result.getServiceDataMap().put(EntityType.APIGateway_API, serviceData1);

        List<ResourceMetadata> resourceMetadataList2 = new ArrayList<>();
        resourceMetadataList2
                .add(new ResourceMetadata(resource1, null));
        resourceMetadataList2
                .add(new ResourceMetadata("6", null));
        resourceMetadataList2
                .add(new ResourceMetadata("7", null));
        ServiceData serviceData2 = new ServiceData(EntityType.AWS_TRANSFER_SERVER, resourceMetadataList2);
        result.getServiceDataMap().put(EntityType.AWS_TRANSFER_SERVER, serviceData2);

        results.add(result);

        GenerateReportInput generateReportInput = GenerateReportInput.builder().results(results)
                .analyzedRegions(List.of(Region.US_EAST_1, Region.EU_WEST_2))
                .executionInput(ExecutionInput.builder().build()).build();
        ReportResult reportResult = htmlReport.generateReport(generateReportInput);
        assertNotNull(reportResult);
        Document doc = Jsoup.parse(reportResult.getReport());
        assertNotNull(doc.body());
        assertTrue(reportResult.getReport().contains(resource1), reportResult.getReport());
    }
}
