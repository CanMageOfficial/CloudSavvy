package com.cloudSavvy.aws.rule;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.redshift.model.Cluster;

import java.util.List;

@Getter
@Builder
public class RuleContext {
    private List<String> dynamoDbTableNames;

    private List<FileSystemDescription> efsFileSystems;

    private List<Cluster> redshiftClusters;

    private List<LoadBalancer> v2LoadBalancers;

    // Calling local buckets because buckets are accessed globally in S3 API
    private List<String> localBuckets;
}
