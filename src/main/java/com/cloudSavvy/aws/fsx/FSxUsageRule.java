package com.cloudSavvy.aws.fsx;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.fsx.model.DataCompressionType;
import software.amazon.awssdk.services.fsx.model.FileSystem;
import software.amazon.awssdk.services.fsx.model.FileSystemType;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FSxUsageRule implements AnalyzerRule {
    private FSxAccessor fsxAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.FSX_FILE_SYSTEM;

    @Override
    public AWSService getAWSService() {
        return AWSService.FSx;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<FileSystem> fileSystems = fsxAccessor.listFileSystem();
        if (CollectionUtils.isNullOrEmpty(fileSystems)) {
            return ruleResult;
        }

        for (FileSystem fileSystem : fileSystems) {
            if (fileSystem.fileSystemType() == FileSystemType.LUSTRE) {
                if (fileSystem.lustreConfiguration().dataCompressionType() == DataCompressionType.NONE) {
                    ruleResult.addIssueData(new IssueData(entityType, fileSystem.fileSystemId(),
                            IssueCode.FSX_LUSTRE_FILE_SYSTEM_HAS_NO_COMPRESSION));
                }
            }
        }

        ruleResult.addServiceData(new ServiceData(entityType, fileSystems.stream()
                .map(fs -> new ResourceMetadata(fs.fileSystemId(), fs.creationTime()))
                .collect(Collectors.toList())));

        // 64 GiB is minimum size for Linux FSx file system, 32 GiB for Windows
        Map<String, FileSystem> fileSystemsLargerThanMinMap = new HashMap<>();
        for (FileSystem fs : fileSystems) {
            if (TimeUtils.getElapsedTimeInDays(fs.creationTime()) < ResourceAge.THIRTY_DAYS) {
                continue;
            }
            if (fs.fileSystemType() == FileSystemType.WINDOWS && fs.storageCapacity() > 32) {
                fileSystemsLargerThanMinMap.put(fs.fileSystemId(), fs);
            } else if (fs.storageCapacity() > 64) {
                fileSystemsLargerThanMinMap.put(fs.fileSystemId(), fs);
            }
        }

        Map<FileSystemType, List<String>> fileSystemTypeIdMap = new HashMap<>();

        for (FileSystem fileSystem : fileSystemsLargerThanMinMap.values()) {
            List<String> fileSystemTypeList =
                    fileSystemTypeIdMap.getOrDefault(fileSystem.fileSystemType(), new ArrayList<>());
            fileSystemTypeList.add(fileSystem.fileSystemId());
            fileSystemTypeIdMap.put(fileSystem.fileSystemType(), fileSystemTypeList);
        }

        fileSystemTypeIdMap.entrySet().parallelStream().forEach(entry -> {
            Map<String, MetricDataResult> metricDataResultMap = getMetricData(entry);
            for (String fileSystemId : entry.getValue()) {
                if (metricDataResultMap.containsKey(fileSystemId)) {
                    MetricDataResult dataResult = metricDataResultMap.get(fileSystemId);
                    Double maxStorage = MetricUtils.getMax(dataResult);
                    if (fileSystemsLargerThanMinMap.containsKey(fileSystemId)) {
                        Double maxStorageInGiB = MetricUtils.convertToGiB(maxStorage);
                        double maxUsedStorageInGib = maxStorageInGiB;
                        Integer fileSystemStorageCap = fileSystemsLargerThanMinMap.get(fileSystemId).storageCapacity();

                        // Lustre reports free space instead of used space
                        if (entry.getKey() == FileSystemType.LUSTRE || entry.getKey() == FileSystemType.WINDOWS) {
                            maxUsedStorageInGib = fileSystemStorageCap - maxStorageInGiB;
                        }

                        if (((maxUsedStorageInGib * 100) / fileSystemStorageCap) < 10) {
                            ruleResult.addIssueData(new IssueData(entityType, fileSystemId,
                                    IssueCode.FSX_FILE_SYSTEM_HAS_LOW_USAGE));
                        }
                    }
                }
            }
        });

        return ruleResult;
    }

    private Map<String, MetricDataResult> getMetricData(final Map.Entry<FileSystemType, List<String>> entry) {
        switch (entry.getKey()) {
            case OPENZFS:
                return cloudWatchAccessor.getFSXUsedStorageCapacityMetricData(entry.getValue());
            case ONTAP:
                return cloudWatchAccessor.getFSXStorageUsedMetricData(entry.getValue());
            case LUSTRE:
                return cloudWatchAccessor.getFSXFreeDataStorageCapMetricData(entry.getValue());
            case WINDOWS:
                return cloudWatchAccessor.getFSXFreeStorageCapMetricData(entry.getValue());
            default:
                return new HashMap<>();
        }
    }
}
