package com.cloudSavvy.aws.ec2;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.ec2.model.ConnectivityType;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class NatGatewayRule implements AnalyzerRule {

    private EC2Accessor ec2Accessor;

    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.NAT_GATEWAY;

    @Override
    public AWSService getAWSService() {
        return AWSService.VPC;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<NatGateway> gateways = ec2Accessor.listNATGateways();
        if (CollectionUtils.isNullOrEmpty(gateways)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, gateways.stream()
                .map(gateway -> new ResourceMetadata(gateway.natGatewayId(), gateway.createTime()))
                .collect(Collectors.toList())));

        for (NatGateway gateway : gateways) {
            if (gateway.connectivityType() == ConnectivityType.PRIVATE) {
                ruleResult.addIssueData(new IssueData(entityType,
                        gateway.natGatewayId(), IssueCode.PRIVATE_NAT_GATEWAY_DETECTED));
            }
        }

        List<NatGateway> oldGateways = gateways.stream()
                .filter(natGateway -> TimeUtils.getElapsedTimeInDays(natGateway.createTime()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        List<String> oldGatewayIds = oldGateways.stream().map(NatGateway::natGatewayId).collect(Collectors.toList());

        analyzeNatGateways(oldGatewayIds, ruleResult);
        return ruleResult;
    }

    private void analyzeNatGateways(final List<String> oldGatewayIds, RuleResult ruleResult) {
        Map<String, MetricDataResult> metricDataResultMap =
                cloudWatchAccessor.getNatGatewayConnectionsMetricData(oldGatewayIds);

        log.debug("functionsNameToDataResultMap: {}", metricDataResultMap);
        for (Map.Entry<String, MetricDataResult> entry : metricDataResultMap.entrySet()) {
            if (MetricUtils.getMax(entry.getValue()) < 1) {
                ruleResult.addIssueData(new IssueData(entityType,
                        entry.getKey(), IssueCode.NAT_GATEWAY_NOT_USED));
            }
        }
    }
}
