package com.cloudSavvy.aws.ecs;

import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.DeploymentRolloutState;
import software.amazon.awssdk.services.ecs.model.LaunchType;
import software.amazon.awssdk.services.ecs.model.Service;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class ECSServiceRule implements AnalyzerRule {

    private ECSAccessor ecsAccessor;

    private final EntityType clusterEntityType = EntityType.ECS_CLUSTER;
    private final EntityType serviceEntityType = EntityType.ECS_CLUSTER_SERVICE;

    @Override
    public AWSService getAWSService() {
        return AWSService.ECS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Cluster> clusters = ecsAccessor.listClusters();
        if (CollectionUtils.isNullOrEmpty(clusters)) {
            return ruleResult;
        }

        List<Cluster> activeClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            if (ECSAccessor.FAILED.equals(cluster.status())) {
                ruleResult.addIssueData(new IssueData(clusterEntityType, cluster.clusterName(),
                        IssueCode.ECS_CLUSTER_IN_FAILED_STATE));
            } else if (ECSAccessor.ACTIVE.equals(cluster.status())) {
                activeClusters.add(cluster);
            }
        }

        if (CollectionUtils.isNullOrEmpty(activeClusters) || ECSUtils.getActiveServiceCount(activeClusters) == 0) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(clusterEntityType, clusters.stream()
                .map(cluster -> new ResourceMetadata(cluster.clusterName(), null))
                .collect(Collectors.toList())));

        List<String> servicesArns = ecsAccessor.listServicesArns();
        if (CollectionUtils.isNullOrEmpty(servicesArns)) {
            return ruleResult;
        }

        List<Service> services = ecsAccessor.describeServices(servicesArns);
        List<Service> servicesToAnalyze = services.stream()
                .filter(service -> TimeUtils.getElapsedTimeInDays(service.createdAt()) > ResourceAge.SEVEN_DAYS
                        && service.launchType() == LaunchType.FARGATE)
                .collect(Collectors.toList());
        if (CollectionUtils.isNullOrEmpty(servicesToAnalyze)) {
            return ruleResult;
        }

        Map<String, List<Service>> clusterNameToServicesMap = ECSUtils.getClusterNameToServicesMap(services);
        for (Map.Entry<String, List<Service>> entry : clusterNameToServicesMap.entrySet()) {
            for (Service service : entry.getValue()) {
                if (getValue(service.desiredCount()) == 0) {
                    if (service.networkConfiguration() != null
                            && service.networkConfiguration().awsvpcConfiguration() != null
                            && service.networkConfiguration().awsvpcConfiguration().assignPublicIp() == AssignPublicIp.ENABLED) {
                        ruleResult.addIssueData(new IssueData(serviceEntityType, entry.getKey(), service.serviceName(),
                                IssueCode.ECS_CLUSTER_SERVICE_HAS_NO_TASK));
                    }
                } else if (getValue(service.pendingCount()) + getValue(service.runningCount())
                        < getValue(service.desiredCount())) {
                    long inProgressDeployments = service.deployments().stream()
                            .filter(deployment -> deployment.rolloutState() == DeploymentRolloutState.IN_PROGRESS).count();
                    if (inProgressDeployments == 0) {
                        ruleResult.addIssueData(new IssueData(serviceEntityType, entry.getKey(), service.serviceName(),
                                IssueCode.ECS_CLUSTER_SERVICE_IS_NOT_RUNNING_ALL_TASKS));
                    }
                }
            }
        }

        return ruleResult;
    }

    private int getValue(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }
}
