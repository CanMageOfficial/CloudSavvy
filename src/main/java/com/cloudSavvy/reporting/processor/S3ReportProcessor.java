package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.reporting.S3RequestObject;
import com.cloudSavvy.reporting.builder.ReportResult;
import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.IssueDataUtils;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class S3ReportProcessor implements ReportProcessor {
    private static final Set<ReportType> SUPPORTED_REPORT_TYPES = ImmutableSet.of(ReportType.ERROR_DATA_HTML,
            ReportType.FULL_ISSUE_DATA_HTML, ReportType.SERVICE_DATA_HTML, ReportType.DAILY_CHARGES_HTML,
            ReportType.INTERNAL_DATA_JSON);
    private final S3Client s3Client;

    @Override
    public ProcessReportResult processReport(final @NonNull ProcessReportInput input) {
        String resultsBucket = input.getOutputBucketName();
        if (StringUtils.isEmpty(resultsBucket)) {
            log.error("Report bucket is not set in environment variables.");
            return ProcessReportResult.builder().build();
        }

        if (input.getReportTypeDataMap().isEmpty()) {
            throw new RuntimeException("No report is provided to S3ReportProcessor");
        }

        List<S3RequestObject> requestObjects = new ArrayList<>();
        Map<ReportType, String> reportLocations = new HashMap<>(SUPPORTED_REPORT_TYPES.size());
        for (Map.Entry<ReportType, ReportResult> entry : input.getReportTypeDataMap().entrySet()) {
            ReportType reportType = entry.getKey();
            if (!SUPPORTED_REPORT_TYPES.contains(reportType)) {
                continue;
            }

            String dataKey = IssueDataUtils.getS3ObjectLocation(reportType, input.getOutputFolderName());

            if (reportType == ReportType.INTERNAL_DATA_JSON) {
                requestObjects.add(S3RequestObject.jsonDataBuilder(entry.getValue().getReport())
                        .bucketName(resultsBucket).key(dataKey).build());
            } else {
                requestObjects.add(S3RequestObject.htmlDataBuilder(entry.getValue().getReport())
                        .bucketName(resultsBucket).key(dataKey).build());
            }

            // This will be reported in lambda logs
            if (reportType == ReportType.FULL_ISSUE_DATA_HTML || reportType == ReportType.SERVICE_DATA_HTML) {
                reportLocations.put(reportType, dataKey);
            }
        }

        requestObjects.parallelStream().forEach(requestObject -> {
            uploadFile(requestObject);
            log.info("Uploaded {} to s3. Bucket name: {}", requestObject.getKey(), requestObject.getBucketName());
        });

        return ProcessReportResult.builder().reportLocationType(ReportLocationType.S3_BUCKET)
                .locations(reportLocations).build();
    }

    private void uploadFile(S3RequestObject requestObject) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(requestObject.getBucketName())
                .contentType(requestObject.getContentType())
                .key(requestObject.getKey())
                .build();
        RequestBody requestBody = requestObject.getRequestBody();
        s3Client.putObject(request, requestBody);
    }
}
