package com.cloudSavvy.aws.secretsmanager;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class SecretsManagerUsageRule implements AnalyzerRule {

    private SecretsManagerAccessor secretsManagerAccessor;

    private final EntityType entityType = EntityType.SECRETS_MANAGER_SECRET;

    @Override
    public AWSService getAWSService() {
        return AWSService.SECRETS_MANAGER;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<SecretListEntry> secretEntries = secretsManagerAccessor.listUnusedSecrets();

        for (SecretListEntry secretEntry : secretEntries) {
            ruleResult.addIssueData(new IssueData(entityType, secretEntry.name(),
                    IssueCode.SECRETS_MANAGER_UNUSED_SECRET));
        }

        return ruleResult;
    }
}
