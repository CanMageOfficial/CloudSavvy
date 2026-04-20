package com.cloudSavvy.aws.bedrock;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.bedrock.model.ProvisionedModelSummary;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class BedrockProvisionedThroughputRule implements AnalyzerRule {

    private BedrockAccessor bedrockAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.BEDROCK_PROVISIONED_THROUGHPUT;

    @Override
    public AWSService getAWSService() {
        return AWSService.Bedrock;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<ProvisionedModelSummary> throughputs = bedrockAccessor.listInServiceProvisionedThroughputs();
        if (CollectionUtils.isNullOrEmpty(throughputs)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, throughputs.stream()
                .map(t -> new ResourceMetadata(t.provisionedModelName(), t.creationTime()))
                .collect(Collectors.toList())));

        Map<String, ProvisionedModelSummary> arnToModel = throughputs.stream()
                .collect(Collectors.toMap(ProvisionedModelSummary::provisionedModelArn, t -> t));

        List<String> arns = throughputs.stream()
                .map(ProvisionedModelSummary::provisionedModelArn)
                .collect(Collectors.toList());

        Map<String, MetricDataResult> invocationMetrics =
                cloudWatchAccessor.getBedrockInvocationsMetricData(arns);
        log.debug("Bedrock invocation metrics: {}", invocationMetrics);

        for (Map.Entry<String, MetricDataResult> entry : invocationMetrics.entrySet()) {
            MetricDataResult data = entry.getValue();
            boolean hasNoInvocations = CollectionUtils.isNullOrEmpty(data.values())
                    || data.values().stream().mapToDouble(Double::doubleValue).sum() == 0;
            if (hasNoInvocations) {
                ProvisionedModelSummary model = arnToModel.get(entry.getKey());
                String name = model != null ? model.provisionedModelName() : entry.getKey();
                ruleResult.addIssueData(new IssueData(entityType, name,
                        IssueCode.BEDROCK_PROVISIONED_THROUGHPUT_NOT_USED));
            }
        }

        return ruleResult;
    }
}
