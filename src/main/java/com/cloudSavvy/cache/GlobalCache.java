package com.cloudSavvy.cache;

import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.aws.s3.BucketRepository;
import com.cloudSavvy.aws.s3.S3Accessor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GlobalCache {
    private List<BucketRepository> bucketRepositories = null;

    public List<BucketRepository> getBucketRepositories(final S3Accessor s3Accessor, final RunMetadata runMetadata) {
        populateBucketMetadata(s3Accessor, runMetadata);
        return bucketRepositories;
    }

    private synchronized void setBucketRepositories(final S3Accessor s3Accessor) {
        if (bucketRepositories == null) {
            log.debug("Retrieving buckets for cache");
            List<String> buckets = s3Accessor.listBuckets();
            bucketRepositories = new ArrayList<>();
            buckets.forEach(bucket -> bucketRepositories.add(new BucketRepository(bucket)));
        }
    }

    private void populateBucketMetadata(final S3Accessor s3Accessor, final RunMetadata runMetadata) {
        setBucketRepositories(s3Accessor);

        // https://github.com/aws/aws-sdk-java/issues/1338
        if (enableRegionLocationDetection(runMetadata)) {
            bucketRepositories.parallelStream().forEach(bucketRepo -> bucketRepo.setBucketLocation(s3Accessor));
        }

        bucketRepositories.parallelStream().forEach(bucketRepo -> {
            try {
                bucketRepo.waitForRegionUpdate();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void invalidate() {
        if (bucketRepositories != null) {
            bucketRepositories.clear();
        }
    }

    private boolean enableRegionLocationDetection(final RunMetadata runMetadata) {
        return runMetadata.getRegion() != Region.US_EAST_1 || runMetadata.getNumberOfRegionsAnalyzed() == 1;
    }
}
