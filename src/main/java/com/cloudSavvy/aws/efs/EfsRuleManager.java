package com.cloudSavvy.aws.efs;

import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import com.cloudSavvy.common.run.RunMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;

import java.util.List;

@AllArgsConstructor
public class EfsRuleManager implements RuleManager {
    private EFSAccessor efsAccessor;

    public RuleContext setup(RunMetadata runMetadata) {
        List<FileSystemDescription> efsFileSystems = efsAccessor.listFileSystems();
        return RuleContext.builder().efsFileSystems(efsFileSystems).build();
    }
}
