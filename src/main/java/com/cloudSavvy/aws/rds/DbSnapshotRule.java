package com.cloudSavvy.aws.rds;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.rds.model.DBClusterSnapshot;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class DbSnapshotRule implements AnalyzerRule {

    private RDSAccessor rdsAccessor;

    @Override
    public AWSService getAWSService() {
        return AWSService.RDS;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DBClusterSnapshot> dbSnapshots = rdsAccessor.listDBClusterSnapshots();

        if (CollectionUtils.isNullOrEmpty(dbSnapshots)) {
            return ruleResult;
        }

        List<DBClusterSnapshot> oldSnapshots = dbSnapshots.stream()
                .filter(snapshot -> TimeUtils.getElapsedTimeInDays(snapshot.snapshotCreateTime()) > ResourceAge.THIRTY_DAYS)
                .collect(Collectors.toList());

        if (CollectionUtils.isNullOrEmpty(oldSnapshots)) {
            return ruleResult;
        }

        Map<String, List<DBClusterSnapshot>> snapshotNameMap = new HashMap<>();
        for (DBClusterSnapshot snapshot : oldSnapshots) {
            List<DBClusterSnapshot> snapshotsList =
                    snapshotNameMap.getOrDefault(snapshot.dbClusterIdentifier(), new ArrayList<>());
            snapshotsList.add(snapshot);
            snapshotNameMap.put(snapshot.dbClusterIdentifier(), snapshotsList);
        }

        for (Map.Entry<String, List<DBClusterSnapshot>> entry : snapshotNameMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                // TODO add error code
                log.info("Multiple snapshots for RDS");
            }
        }

        return ruleResult;
    }
}
