package com.cloudSavvy.aws.s3;

import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketLocationConstraint;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusResponse;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.LoggingEnabled;
import software.amazon.awssdk.services.s3.model.MetricsConfiguration;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class S3Accessor {
    private final S3Client s3Client;

    public List<String> listBuckets() {
        List<String> buckets = new ArrayList<>();

        ListBucketsResponse bucketsResponse = s3Client.listBuckets();
        for (Bucket bucket : bucketsResponse.buckets()) {
            if (TimeUtils.getElapsedTimeInDays(bucket.creationDate()) > ResourceAge.SEVEN_DAYS) {
                buckets.add(bucket.name());
            }
        }

        log.debug("buckets: {}", buckets);
        return buckets;
    }

    public Region getBucketLocation(String bucketName) {
        GetBucketLocationRequest request = GetBucketLocationRequest.builder()
                .bucket(bucketName).build();
        GetBucketLocationResponse bucketLocationResponse = s3Client.getBucketLocation(request);
        if (bucketLocationResponse.locationConstraint() == BucketLocationConstraint.UNKNOWN_TO_SDK_VERSION) {
            return Region.US_EAST_1;
        }
        String regionId = bucketLocationResponse.locationConstraint()
                .name().toLowerCase().replace('_', '-');
        return Region.of(regionId);
    }

    public List<MetricsConfiguration> listBucketMetricConfigs(String bucketName) {
        ListBucketMetricsConfigurationsRequest request = ListBucketMetricsConfigurationsRequest.builder()
                .bucket(bucketName).build();
        ListBucketMetricsConfigurationsResponse response = s3Client.listBucketMetricsConfigurations(request);
        return response.metricsConfigurationList();
    }

    public List<Tag> listBucketTags(String bucketName) {
        try {
            GetBucketTaggingRequest request = GetBucketTaggingRequest.builder()
                    .bucket(bucketName).build();
            GetBucketTaggingResponse response = s3Client.getBucketTagging(request);
            return response.tagSet();
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                log.error("listBucketTags returned unknown error", e);
                throw e;
            }
            return new ArrayList<>();
        }
    }

    public boolean hasLifecycleConfiguration(String bucketName) {
        try {
            GetBucketLifecycleConfigurationRequest request = GetBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucketName).build();
            GetBucketLifecycleConfigurationResponse response = s3Client.getBucketLifecycleConfiguration(request);
            return !CollectionUtils.isNullOrEmpty(response.rules());
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                log.error("hasLifecycleConfiguration returned unknown error", e);
                throw e;
            }
            return false;
        }
    }

    public LoggingEnabled getBucketLoggingEnabled(String bucketName) {
        try {
            GetBucketLoggingRequest request = GetBucketLoggingRequest.builder()
                    .bucket(bucketName).build();
            GetBucketLoggingResponse response = s3Client.getBucketLogging(request);
            return response.loggingEnabled();
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                log.error("getBucketLogging returned unknown error", e);
            }
        }
        return null;
    }

    public Boolean isBucketPublic(String bucketName) {
        try {
            GetBucketPolicyStatusRequest request = GetBucketPolicyStatusRequest.builder()
                    .bucket(bucketName).build();
            GetBucketPolicyStatusResponse response = s3Client.getBucketPolicyStatus(request);
            return response.policyStatus().isPublic();
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                log.error("getBucketWebsite returned unknown error", e);
            }
        }
        return false;
    }
}
