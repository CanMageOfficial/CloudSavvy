package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.Disk;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailDiskRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_DISK;

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Disk> disks = lightsailAccessor.listDisks();

        if (CollectionUtils.isNullOrEmpty(disks)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, disks.stream()
                .map(disk -> new ResourceMetadata(disk.name(), disk.createdAt()))
                .collect(Collectors.toList())));

        List<Disk> oldDisks = disks.stream()
                .filter(disk -> TimeUtils.getElapsedTimeInDays(disk.createdAt()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        for (Disk disk : oldDisks) {
            if (!disk.isAttached()) {
                ruleResult.addIssueData(new IssueData(entityType,
                        disk.name(), IssueCode.LIGHTSAIL_DISK_IS_NOT_ATTACHED));
            }
        }

        return ruleResult;
    }
}
