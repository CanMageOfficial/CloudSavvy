package com.cloudSavvy.aws.s3;

import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class S3RuleManager implements RuleManager {
    private S3Accessor s3Accessor;
    private GlobalCache globalCache;

    public RuleContext setup(final RunMetadata runMetadata) {
        List<String> localBuckets = Collections.synchronizedList(new ArrayList<>());
        RuleContext ruleContext = RuleContext.builder().localBuckets(localBuckets).build();

        List<BucketRepository> bucketRepos = globalCache.getBucketRepositories(s3Accessor, runMetadata);

        if (CollectionUtils.isNullOrEmpty(bucketRepos)) {
            return ruleContext;
        }

        bucketRepos.forEach(bucketRepository -> {
            if (bucketRepository.getBucketRegion() == runMetadata.getRegion()) {
                localBuckets.add(bucketRepository.getBucketName());
            }
        });

        return ruleContext;
    }
}
