package com.cloudSavvy.aws.s3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class BucketRepository {
    @Getter
    private final String bucketName;

    @Getter
    private Region bucketRegion;
    private final Lock locationLock = new ReentrantLock();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public BucketRepository(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBucketLocation(S3Accessor s3Accessor) {
        if (bucketRegion != null) {
            return;
        }

        if (locationLock.tryLock()) {
            try {
                log.debug("Retrieving bucket location for bucket {}", bucketName);
                try {
                    this.bucketRegion = s3Accessor.getBucketLocation(bucketName);
                } catch (S3Exception s3Exception) {
                    if (s3Exception.awsErrorDetails() == null
                            || !"AuthorizationHeaderMalformed".equals(s3Exception.awsErrorDetails().errorCode())) {
                        log.error("Retrieving bucket location is failed for bucket {}", bucketName, s3Exception);
                        throw s3Exception;
                    }
                }
            } finally {
                countDownLatch.countDown();
                locationLock.unlock();
            }
        }
    }

    public void waitForRegionUpdate() throws InterruptedException {
        boolean result = countDownLatch.await(1, TimeUnit.MINUTES);
        if (!result) {
            log.error("countDownLatch wait failed.");
        }
    }
}
