package com.cloudSavvy.aws.transfer;

import com.cloudSavvy.aws.cloudwatch.CloudWatchLogAccessor;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.transfer.model.IdentityProviderType;
import software.amazon.awssdk.services.transfer.model.ListedServer;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AWSTransferServerRule implements AnalyzerRule {
    private AWSTransferAccessor transferAccessor;
    private CloudWatchLogAccessor cloudWatchLogAccessor;

    private final EntityType entityType = EntityType.AWS_TRANSFER_SERVER;
    private final String transferLogGroupPrefix = "/aws/transfer/";

    @Override
    public AWSService getAWSService() {
        return AWSService.AWS_Transfer;
    }

    @Override
    public RuleResult call(final RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<ListedServer> servers = transferAccessor.listServers();

        if (CollectionUtils.isNullOrEmpty(servers)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, servers.stream()
                .map(server -> new ResourceMetadata(server.serverId(), null))
                .collect(Collectors.toList())));

        List<LogGroup> logGroups = cloudWatchLogAccessor.getLogGroups(transferLogGroupPrefix);
        Map<String, LogGroup> logGroupNameMap =
                logGroups.stream().collect(Collectors.toMap(LogGroup::logGroupName, Function.identity()));

        List<ListedServer> serversWithUser = new ArrayList<>();
        for (ListedServer server : servers) {
            if (server.identityProviderType() == IdentityProviderType.SERVICE_MANAGED && server.userCount() == 0) {
                ruleResult.addIssueData(new IssueData(entityType, server.serverId(),
                        IssueCode.AWS_TRANSFER_SERVER_HAS_NO_USER));
            } else {
                serversWithUser.add(server);
            }
        }

        List<ListedServer> runningServers = new ArrayList<>();
        for (ListedServer server : serversWithUser) {
            if (!logGroupNameMap.containsKey(transferLogGroupPrefix + server.serverId())) {
                ruleResult.addIssueData(new IssueData(entityType, server.serverId(),
                        IssueCode.AWS_TRANSFER_SERVER_NOT_USED));
            } else {
                runningServers.add(server);
            }
        }

        runningServers.parallelStream().forEach(server -> {
            boolean isUnused = false;
            Optional<LogStream> logStream = cloudWatchLogAccessor
                    .getLogStreamByEventTime(transferLogGroupPrefix + server.serverId());
            if (logStream.isPresent()) {
                long lastEventTime = logStream.get().lastEventTimestamp();
                if (TimeUtils.getElapsedTimeInDays(lastEventTime) > ResourceAge.SEVEN_DAYS) {
                    isUnused = true;
                }
            } else {
                isUnused = true;
            }
            if (isUnused) {
                ruleResult.addIssueData(new IssueData(entityType, server.serverId(),
                        IssueCode.AWS_TRANSFER_SERVER_NOT_USED));
            }
        });

        return ruleResult;
    }
}
