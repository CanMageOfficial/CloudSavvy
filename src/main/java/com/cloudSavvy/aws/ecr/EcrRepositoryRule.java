package com.cloudSavvy.aws.ecr;

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
import com.cloudSavvy.utils.CdkUtils;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ecr.model.ImageDetail;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class EcrRepositoryRule implements AnalyzerRule {

    private EcrAccessor ecrAccessor;

    private final EntityType entityType = EntityType.ECR_REPOSITORY;

    @Override
    public AWSService getAWSService() {
        return AWSService.ECR;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<Repository> repositories = ecrAccessor.listRepositories();
        if (CollectionUtils.isNullOrEmpty(repositories)) {
            return ruleResult;
        }

        List<Repository> filteredRepositories = repositories.stream()
                .filter(r -> !CdkUtils.isCdkAssetRepository(r.repositoryName()))
                .collect(Collectors.toList());

        ruleResult.addServiceData(new ServiceData(entityType, filteredRepositories.stream()
                .map(r -> new ResourceMetadata(r.repositoryName(), r.createdAt()))
                .collect(Collectors.toList())));

        for (Repository repo : filteredRepositories) {
            if (TimeUtils.getElapsedTimeInDays(repo.createdAt()) < ResourceAge.THIRTY_DAYS) {
                continue;
            }
            try {
                List<ImageDetail> images = ecrAccessor.listImages(repo.repositoryName());
                if (CollectionUtils.isNullOrEmpty(images)) {
                    ruleResult.addIssueData(new IssueData(entityType, repo.repositoryName(),
                            IssueCode.ECR_REPOSITORY_NOT_USED));
                    continue;
                }
                ImageDetail latest = images.get(0);
                if (latest.imagePushedAt() != null &&
                        TimeUtils.getElapsedTimeInDays(latest.imagePushedAt()) > ResourceAge.THREE_MONTHS) {
                    ruleResult.addIssueData(new IssueData(entityType, repo.repositoryName(),
                            IssueCode.ECR_REPOSITORY_NOT_USED));
                }
            } catch (Exception e) {
                log.warn("Failed to describe images for ECR repo {}: {}", repo.repositoryName(), e.getMessage());
            }
        }

        return ruleResult;
    }
}
