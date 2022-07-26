package com.cloudSavvy.utils;

import com.cloudSavvy.aws.common.EntityLinks;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.reporting.builder.GenerateReportInput;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.StringUtils;

import java.time.Duration;

@AllArgsConstructor
public class S3UrlBuilder {

    private final S3Presigner preSigner;

    public PresignedGetObjectRequest createPreSignedUrl(String bucketName, String keyName) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest preSignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(objectRequest)
                .build();

        return preSigner.presignGetObject(preSignRequest);
    }

    public String buildPrivateS3Url(GenerateReportInput generateReportInput, ReportType reportType) {
        if (!StringUtils.isEmpty(generateReportInput.getOutputFolderName())
                && !StringUtils.isEmpty(generateReportInput.getExecutionInput().getOutputBucketName())
                && !StringUtils.isEmpty(generateReportInput.getExecutionInput().getRunningRegion())) {
            String s3AnalyzedResourcesLoc = IssueDataUtils.getS3ObjectLocation(reportType,
                    generateReportInput.getOutputFolderName());
            return EntityLinks.getS3ObjectPrefixLink(generateReportInput.getExecutionInput().getOutputBucketName(),
                    generateReportInput.getExecutionInput().getRunningRegion(), s3AnalyzedResourcesLoc);
        }

        return null;
    }

    public String buildPresignedS3Url(GenerateReportInput generateReportInput, ReportType reportType) {
        if (!StringUtils.isEmpty(generateReportInput.getOutputFolderName())
                && !StringUtils.isEmpty(generateReportInput.getExecutionInput().getOutputBucketName())
                && !StringUtils.isEmpty(generateReportInput.getExecutionInput().getRunningRegion())) {
            String s3ObjectKey = IssueDataUtils.getS3ObjectLocation(reportType,
                    generateReportInput.getOutputFolderName());
            PresignedGetObjectRequest presignedGetObjectRequest =
                    createPreSignedUrl(generateReportInput.getExecutionInput().getOutputBucketName(), s3ObjectKey);
            return presignedGetObjectRequest.url().toString();
        }

        return null;
    }
}
