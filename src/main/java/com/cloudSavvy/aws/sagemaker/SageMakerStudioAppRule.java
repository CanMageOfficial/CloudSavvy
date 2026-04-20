package com.cloudSavvy.aws.sagemaker;

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
import software.amazon.awssdk.services.sagemaker.model.AppDetails;
import software.amazon.awssdk.services.sagemaker.model.AppType;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class SageMakerStudioAppRule implements AnalyzerRule {

    private SageMakerAccessor sageMakerAccessor;

    private final EntityType entityType = EntityType.SAGEMAKER_STUDIO_APP;

    @Override
    public AWSService getAWSService() {
        return AWSService.Amazon_SageMaker;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<AppDetails> apps = sageMakerAccessor.listInServiceStudioApps();
        if (CollectionUtils.isNullOrEmpty(apps)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(entityType, apps.stream()
                .map(a -> new ResourceMetadata(appDisplayName(a), a.creationTime()))
                .collect(Collectors.toList())));

        for (AppDetails app : apps) {
            if (app.appType() == AppType.TENSOR_BOARD || app.appType() == AppType.JUPYTER_SERVER) {
                continue;
            }
            if (TimeUtils.getElapsedTimeInDays(app.creationTime()) > ResourceAge.SEVEN_DAYS) {
                ruleResult.addIssueData(new IssueData(entityType, appDisplayName(app),
                        IssueCode.SAGEMAKER_STUDIO_APP_RUNNING_IDLE));
            }
        }

        return ruleResult;
    }

    private String appDisplayName(AppDetails app) {
        String profile = app.userProfileName() != null ? app.userProfileName() : app.spaceName();
        return profile + "/" + app.appName();
    }
}
