package com.cloudSavvy.aws.efs;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.MountTargetDescription;
import software.amazon.awssdk.services.efs.model.ThroughputMode;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class EFSFileSystemUsageRule implements AnalyzerRule {

    private EFSAccessor efsAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.EFS_FILE_SYSTEMS;

    @Override
    public AWSService getAWSService() {
        return AWSService.EFS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<FileSystemDescription> fileSystems = ruleContext.getEfsFileSystems();
        if (CollectionUtils.isNullOrEmpty(fileSystems)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, fileSystems.stream()
                .map(fileSystem -> new ResourceMetadata(fileSystem.fileSystemId(), fileSystem.creationTime()))
                .collect(Collectors.toList())));

        List<FileSystemDescription> oldFileSystems = fileSystems.stream()
                .filter(fileSystem -> TimeUtils.getElapsedTimeInDays(fileSystem.creationTime()) > ResourceAge.SEVEN_DAYS)
                .filter(fileSystem -> fileSystem.throughputMode() == ThroughputMode.PROVISIONED)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldFileSystems)) {
            return ruleResult;
        }

        List<FileSystemDescription> fileSystemsWithMount = Collections.synchronizedList(new ArrayList<>());
        oldFileSystems.parallelStream().forEach(filesystem -> {
            List<MountTargetDescription> mountTargets = efsAccessor.listMountTargets(filesystem.fileSystemId());
            if (CollectionUtils.isNullOrEmpty(mountTargets)) {
                ruleResult.addIssueData(new IssueData(entityType,
                        filesystem.fileSystemId(), IssueCode.EFS_FILE_SYSTEM_HAS_NO_MOUNT_TARGET));
            } else {
                fileSystemsWithMount.add(filesystem);
            }
        });

        Map<String, FileSystemDescription> fileSystemIdMap = fileSystemsWithMount.stream()
                .collect(Collectors.toMap(FileSystemDescription::fileSystemId, Function.identity()));

        analyzeEFSFileSystems(new ArrayList<>(fileSystemIdMap.keySet()), ruleResult);

        return ruleResult;
    }

    private void analyzeEFSFileSystems(final List<String> fileSystemIds, RuleResult ruleResult) {
        Map<String, MetricDataResult> metricDataMap =
                cloudWatchAccessor.getEFSClientConnectionsMetricData(fileSystemIds);

        for (Map.Entry<String, MetricDataResult> entry : metricDataMap.entrySet()) {
            if (MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.EFS_FILE_SYSTEM_NOT_USED));
            }
        }
    }
}
