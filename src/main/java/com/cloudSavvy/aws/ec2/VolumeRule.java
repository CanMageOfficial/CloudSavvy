package com.cloudSavvy.aws.ec2;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeState;
import software.amazon.awssdk.services.ec2.model.VolumeStatusInfoStatus;
import software.amazon.awssdk.services.ec2.model.VolumeStatusItem;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class VolumeRule implements AnalyzerRule {

    private EC2Accessor ec2Accessor;
    private final EntityType entityType = EntityType.EBS_VOLUME;

    @Override
    public AWSService getAWSService() {
        return AWSService.EC2;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Volume> volumes = ec2Accessor.listVolumes();
        if (CollectionUtils.isNullOrEmpty(volumes)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, volumes.stream()
                .map(volume -> new ResourceMetadata(volume.volumeId(), volume.createTime()))
                .collect(Collectors.toList())));

        Map<String, VolumeStatusItem> volumeIdToStatusMap = ec2Accessor.listVolumeStatuses();

        for (Volume volume : volumes) {
            if (volume.state() == VolumeState.AVAILABLE) {
                ruleResult.addIssueData(new IssueData(entityType,
                        volume.volumeId(), IssueCode.EBS_VOLUME_UNATTACHED));
            } else if (volume.state() == VolumeState.ERROR) {
                ruleResult.addIssueData(new IssueData(entityType,
                        volume.volumeId(), IssueCode.EBS_VOLUME_IN_ERROR));
            } else if (volumeIdToStatusMap.containsKey(volume.volumeId())) {
                VolumeStatusItem volumeStatusItem = volumeIdToStatusMap.get(volume.volumeId());
                if (volumeStatusItem.volumeStatus().status() == VolumeStatusInfoStatus.IMPAIRED) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            volume.volumeId(), IssueCode.EBS_VOLUME_STATUS_IMPAIRED));
                }
            }
        }

        return ruleResult;
    }
}
