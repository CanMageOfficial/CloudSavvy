package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.model.StaticIp;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LightsailStaticIPRule implements AnalyzerRule {

    private LightsailAccessor lightsailAccessor;

    private final EntityType entityType = EntityType.LightSail_STATIC_IP;

    @Override
    public AWSService getAWSService() {
        return AWSService.Lightsail;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<StaticIp> staticIps = lightsailAccessor.listUnAttachedStaticIps();

        if (CollectionUtils.isNullOrEmpty(staticIps)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, staticIps.stream()
                .map(staticIp -> new ResourceMetadata(staticIp.name(), staticIp.createdAt()))
                .collect(Collectors.toList())));

        List<StaticIp> oldStaticIPs = staticIps.stream()
                .filter(staticIp -> TimeUtils.getElapsedTimeInDays(staticIp.createdAt()) > ResourceAge.SEVEN_DAYS)
                .collect(Collectors.toList());

        for (StaticIp staticIp : oldStaticIPs) {
            ruleResult.addIssueData(new IssueData(entityType,
                    staticIp.name(), IssueCode.LIGHTSAIL_STATIC_IP_IS_NOT_ATTACHED));
        }

        return ruleResult;
    }
}
