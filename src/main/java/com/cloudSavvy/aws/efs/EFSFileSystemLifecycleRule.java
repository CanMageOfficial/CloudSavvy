package com.cloudSavvy.aws.efs;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.LifecyclePolicy;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class EFSFileSystemLifecycleRule implements AnalyzerRule {

    private EFSAccessor efsAccessor;

    private final EntityType entityType = EntityType.EFS_FILE_SYSTEMS;

    @Override
    public AWSService getAWSService() {
        return AWSService.EFS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<FileSystemDescription> fileSystems = ruleContext.getEfsFileSystems();

        fileSystems.parallelStream().forEach(filesystem -> {
            List<LifecyclePolicy> lifecyclePolicies = efsAccessor.listLifecyclePolicies(filesystem.fileSystemId());
            if (CollectionUtils.isNullOrEmpty(lifecyclePolicies)) {
                ruleResult.addIssueData(new IssueData(entityType,
                        filesystem.fileSystemId(), IssueCode.EFS_FILE_SYSTEM_HAS_NO_LIFECYCLE_POLICY));
            } else {
                Optional<LifecyclePolicy> transitionToIA =
                        lifecyclePolicies.stream().filter(policy -> policy.transitionToIA() != null).findAny();
                if (transitionToIA.isEmpty()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            filesystem.fileSystemId(), IssueCode.EFS_FILE_SYSTEM_MISSING_TRANSITION_TO_IA));
                }

                Optional<LifecyclePolicy> transitionToPrimary =
                        lifecyclePolicies.stream().filter(policy -> policy.transitionToPrimaryStorageClass() != null).findAny();
                if (transitionToPrimary.isEmpty()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            filesystem.fileSystemId(), IssueCode.EFS_FILE_SYSTEM_MISSING_TRANSITION_TO_PRIMARY_STORAGE));
                }
            }
        });

        return ruleResult;
    }
}
