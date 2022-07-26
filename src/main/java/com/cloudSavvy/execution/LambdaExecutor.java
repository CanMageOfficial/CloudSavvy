package com.cloudSavvy.execution;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cloudSavvy.dagger.DaggerExecutorComponent;
import com.cloudSavvy.localization.LocalizationReader;
import com.cloudSavvy.model.StartRunRequest;
import com.cloudSavvy.model.StartRunResponse;
import com.cloudSavvy.reporting.processor.ProcessReportResult;
import com.cloudSavvy.utils.RegionUtils;
import com.cloudSavvy.validator.EmailValidator;
import com.cloudSavvy.aws.common.EntityLinks;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.EnvironmentUtils;
import com.yworks.util.annotation.Obfuscation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import software.amazon.awssdk.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Obfuscation()
@Slf4j
@AllArgsConstructor
public class LambdaExecutor implements RequestHandler<StartRunRequest, StartRunResponse> {

    private final GlobalExecutor globalExecutor;
    public LambdaExecutor() {
        globalExecutor = DaggerExecutorComponent.create().getGlobalExecutor();
    }

    @SneakyThrows
    @Override
    public StartRunResponse handleRequest(StartRunRequest request, Context context) {

        if (EnvironmentUtils.isDebug()) {
            Configurator.setRootLevel(Level.DEBUG);
        } else {
            Configurator.setRootLevel(Level.INFO);
        }

        boolean reportAllResources = EnvironmentUtils.isReportAllResources();
        String runRegions = EnvironmentUtils.getRunRegions();
        String awsAccountId = EnvironmentUtils.getAWSAccountId();
        String stackName = EnvironmentUtils.getStackName();
        String lambdaRegion = EnvironmentUtils.getLambdaRegion();
        String resultsBucketName = EnvironmentUtils.getResultsBucket();
        String stackPath = null;

        if (!StringUtils.isEmpty(stackName)) {
            stackPath = EntityLinks.getEntityLink(RegionUtils.parseRegion(lambdaRegion), EntityType.STACK, stackName);
        }

        validateEmailAddresses();

        ExecutionInput input = ExecutionInput.builder().requestedRegions(RegionUtils.parseInputRegions(runRegions))
                .reportAllResources(reportAllResources).awsAccountId(awsAccountId)
                .stackPath(stackPath).runningRegion(lambdaRegion).outputBucketName(resultsBucketName).build();
        ExecutionResult result = globalExecutor.execute(input);
        ResourceBundle resourceBundle = LocalizationReader.getMessageTexts(Locale.US);

        Map<String, String> s3ReportLocations = new HashMap<>();
        result.getReportResults().stream()
                .filter(reportResult -> reportResult.getReportLocationType() == ReportLocationType.S3_BUCKET)
                .forEach(reportResult -> {
                    for (Map.Entry<ReportType, String> locEntry : reportResult.getLocations().entrySet()) {
                        String s3Location =
                                EntityLinks.getS3ObjectPrefixLink(resultsBucketName, lambdaRegion, locEntry.getValue());
                        s3ReportLocations.put(resourceBundle.getString(locEntry.getKey().name()), s3Location);
                    }
                });

        List<String> emailReportReceivers = result.getReportResults().stream()
                .filter(location -> location.getReportLocationType() == ReportLocationType.EMAIL)
                .map(ProcessReportResult::getEmailAddresses).flatMap(List::stream).collect(Collectors.toList());

        return StartRunResponse.builder().s3ReportLocations(s3ReportLocations)
                .emailReportReceivers(emailReportReceivers).build();
    }

    private void validateEmailAddresses() {
        EmailValidator.validateToEmailAddresses(EnvironmentUtils.getToEmailAddresses());
        EmailValidator.validateFromEmail(EnvironmentUtils.getFromEMailAddress());
    }
}
