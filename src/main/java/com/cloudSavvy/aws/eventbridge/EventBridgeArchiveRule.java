package com.cloudSavvy.aws.eventbridge;

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
import software.amazon.awssdk.services.eventbridge.model.Archive;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class EventBridgeArchiveRule implements AnalyzerRule {

    private EventBridgeAccessor eventBridgeAccessor;

    private final EntityType entityType = EntityType.EVENTBRIDGE_ARCHIVE;

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_EventBridge;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<Archive> archives = eventBridgeAccessor.listArchives();

        if (CollectionUtils.isNullOrEmpty(archives)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, archives.stream()
                .map(archive -> new ResourceMetadata(archive.archiveName(), archive.creationTime()))
                .collect(Collectors.toList())));

        for (Archive archive : archives) {
            if (archive.retentionDays() == 0) {
                ruleResult.addIssueData(new IssueData(entityType, archive.archiveName(),
                        IssueCode.EVENTBRIDGE_ARCHIVE_HAS_NO_RETENTION));
            }
        }

        return ruleResult;
    }
}
