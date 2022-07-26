package com.cloudSavvy.aws.glue;

import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.glue.model.DevEndpoint;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GlueDevEndPointRule implements AnalyzerRule {
    private GlueAccessor glueAccessor;

    private final EntityType entityType = EntityType.GLUE_DEV_ENDPOINT;

    private static final String TERMINATING = "TERMINATING";
    private static final String TERMINATED = "TERMINATED";

    @Override
    public AWSService getAWSService() {
        return AWSService.GLUE;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<String> endpointNames = glueAccessor.listDevEndPoints();
        if (CollectionUtils.isNullOrEmpty(endpointNames)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, endpointNames.stream()
                .map(endpointName -> new ResourceMetadata(endpointName, null))
                .collect(Collectors.toList())));

        List<DevEndpoint> devEndpoints = glueAccessor.getDevEndPoints(endpointNames);
        List<DevEndpoint> oldDevEndpoints = devEndpoints.stream().filter(endpoint ->
                        TimeUtils.getElapsedTimeInDays(endpoint.createdTimestamp()) > ResourceAge.SEVEN_DAYS)
                .filter(endpoint -> !TERMINATING.equals(endpoint.status())
                        && !TERMINATED.equals(endpoint.status())).collect(Collectors.toList());

        for (DevEndpoint endpoint : oldDevEndpoints) {
            ruleResult.addIssueData(new IssueData(entityType, endpoint.endpointName(),
                    IssueCode.GLUE_DEVELOPMENT_ENDPOINT_FOUND));
        }

        return ruleResult;
    }
}
