package com.cloudSavvy.aws.ec2;

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
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ElasticIpAddressRule implements AnalyzerRule {

    private EC2Accessor ec2Accessor;

    private final EntityType entityType = EntityType.ELASTIC_IP_ADDRESS;

    @Override
    public AWSService getAWSService() {
        return AWSService.VPC;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult result = new RuleResult();
        List<Address> addresses = ec2Accessor.listAddresses();
        if (CollectionUtils.isNullOrEmpty(addresses)) {
            return result;
        }

        result.addServiceData(new ServiceData(entityType, addresses.stream()
                .map(address -> new ResourceMetadata(address.allocationId(), null))
                .collect(Collectors.toList())));

        for (Address address : addresses) {
            if (StringUtils.isEmpty(address.associationId())) {
                result.addIssueData(new IssueData(entityType,
                        address.allocationId(), IssueCode.ELASTIC_IP_ADDRESS_IS_NOT_ASSOCIATED));
            }
        }
        return result;
    }
}
