package com.cloudSavvy.aws.redshift;

import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleManager;
import com.cloudSavvy.common.run.RunMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.redshift.model.Cluster;

import java.util.List;

@AllArgsConstructor
public class RedshiftRuleManager implements RuleManager {
    private RedshiftAccessor redshiftAccessor;

    public RuleContext setup(RunMetadata runMetadata) {
        List<Cluster> redshiftClusters = redshiftAccessor.listClusters();
        return RuleContext.builder().redshiftClusters(redshiftClusters).build();
    }
}
