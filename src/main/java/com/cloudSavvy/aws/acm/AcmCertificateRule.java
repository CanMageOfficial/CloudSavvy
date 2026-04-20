package com.cloudSavvy.aws.acm;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.acm.model.CertificateSummary;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class AcmCertificateRule implements AnalyzerRule {

    private static final int EXPIRY_WARNING_DAYS = 30;

    private AcmAccessor acmAccessor;

    private final EntityType entityType = EntityType.ACM_CERTIFICATE;

    @Override
    public AWSService getAWSService() {
        return AWSService.ACM;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<CertificateSummary> certificates = acmAccessor.listIssuedCertificates();
        if (CollectionUtils.isNullOrEmpty(certificates)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, certificates.stream()
                .map(c -> new ResourceMetadata(c.domainName(), c.createdAt()))
                .collect(Collectors.toList())));

        Instant now = Instant.now();

        for (CertificateSummary cert : certificates) {
            String name = cert.domainName();

            if (cert.notAfter() != null) {
                long daysUntilExpiry = (cert.notAfter().getEpochSecond() - now.getEpochSecond()) / 86400;
                if (daysUntilExpiry <= EXPIRY_WARNING_DAYS) {
                    ruleResult.addIssueData(new IssueData(entityType, name,
                            IssueCode.ACM_CERTIFICATE_EXPIRING_SOON));
                }
            }

            Boolean inUse = cert.inUse();
            if (Boolean.FALSE.equals(inUse)) {
                Instant issuedAt = cert.issuedAt() != null ? cert.issuedAt() : cert.createdAt();
                if (issuedAt != null && TimeUtils.getElapsedTimeInDays(issuedAt) > ResourceAge.THIRTY_DAYS) {
                    ruleResult.addIssueData(new IssueData(entityType, name,
                            IssueCode.ACM_CERTIFICATE_UNUSED));
                }
            }
        }

        return ruleResult;
    }
}
