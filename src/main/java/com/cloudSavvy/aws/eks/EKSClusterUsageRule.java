package com.cloudSavvy.aws.eks;

import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.ClusterStatus;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class EKSClusterUsageRule implements AnalyzerRule {

    private EKSAccessor eksAccessor;

    private final EntityType entityType = EntityType.EKS_CLUSTER;

    @Override
    public AWSService getAWSService() {
        return AWSService.EKS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> clusterNames = eksAccessor.listClusters();

        if (CollectionUtils.isNullOrEmpty(clusterNames)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, clusterNames.stream()
                .map(clusterName -> new ResourceMetadata(clusterName, null))
                .collect(Collectors.toList())));

        List<Cluster> clusters = Collections.synchronizedList(new ArrayList<>());
        clusterNames.stream().parallel().forEach(clusterName -> {
            Cluster cluster = eksAccessor.getCluster(clusterName);
            if (cluster.status() != ClusterStatus.DELETING) {
                clusters.add(cluster);
            }
        });

        for (Cluster cluster : clusters) {
            if (cluster.status() == ClusterStatus.FAILED) {
                ruleResult.addIssueData(new IssueData(entityType,
                        cluster.name(), IssueCode.EKS_CLUSTER_HAS_FAILED_STATUS));
            }
        }

        Map<String, Cluster> oldClusters = clusters.stream()
                .filter(cluster -> TimeUtils.getElapsedTimeInDays(cluster.createdAt()) > ResourceAge.SEVEN_DAYS)
                .filter(cluster -> cluster.status() != ClusterStatus.FAILED)
                .collect(Collectors.toMap(Cluster::name, Function.identity()));
        List<String> oldClusterNames = new ArrayList<>(oldClusters.keySet());

        Map<String, Integer> clusterFargateProfilesMap = new ConcurrentHashMap<>();
        oldClusterNames.stream().parallel().forEach(clusterName ->
                clusterFargateProfilesMap.put(clusterName, eksAccessor.getFargateProfileCount(clusterName)));

        Map<String, Integer> clusterNodeGroupsMap = new ConcurrentHashMap<>();
        oldClusterNames.stream().parallel().forEach(clusterName ->
                clusterNodeGroupsMap.put(clusterName, eksAccessor.getNodeGroupsCount(clusterName)));

        for (String clusterName : oldClusterNames) {
            if (getCount(clusterFargateProfilesMap, clusterName) == 0 && getCount(clusterNodeGroupsMap, clusterName) == 0) {
                ruleResult.addIssueData(new IssueData(entityType,
                        clusterName, IssueCode.EKS_CLUSTER_HAS_NO_FARGATE_AND_NODEGROUP));
            }
        }

        return ruleResult;
    }

    private int getCount(final Map<String, Integer> countMap, final String key) {
        if (countMap.containsKey(key)) {
            return countMap.get(key);
        }
        return 0;
    }
}
