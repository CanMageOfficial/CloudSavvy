package com.cloudSavvy.aws.lambda;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
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
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.lambda.model.Architecture;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ProvisionedConcurrencyConfigListItem;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class LambdaFunctionUsageRule implements AnalyzerRule {

    private LambdaAccessor lambdaAccessor;

    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.LAMBDA_FUNCTION;
    private static final String AWS_NODEJS_CONNECTION_REUSE_ENABLED = "AWS_NODEJS_CONNECTION_REUSE_ENABLED";

    private static final ImmutableSet<Runtime> ARM64_SUPPORTED_RUNTIMES = ImmutableSet.<Runtime>builder()
            .add(Runtime.NODEJS12_X).add(Runtime.NODEJS14_X).add(Runtime.NODEJS16_X)
            .add(Runtime.NODEJS18_X).add(Runtime.NODEJS20_X)
            .add(Runtime.RUBY2_7).add(Runtime.RUBY3_2).add(Runtime.RUBY3_3)
            .add(Runtime.PYTHON3_8).add(Runtime.PYTHON3_9).add(Runtime.PYTHON3_10).add(Runtime.PYTHON3_12).build();

    private static final ImmutableSet<Runtime> NODEJS_RUNTIMES = ImmutableSet.<Runtime>builder()
            .add(Runtime.NODEJS12_X).add(Runtime.NODEJS14_X).add(Runtime.NODEJS16_X)
            .add(Runtime.NODEJS18_X).add(Runtime.NODEJS20_X).build();

    private static final ImmutableSet<Runtime> DEPRECATED_RUNTIMES = ImmutableSet.<Runtime>builder()
            .add(Runtime.NODEJS16_X).add(Runtime.PYTHON3_8).add(Runtime.DOTNET6).build();

    @Override
    public AWSService getAWSService() {
        return AWSService.Lambda;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();

        List<FunctionConfiguration> functions = lambdaAccessor.listFunctions();
        if (CollectionUtils.isNullOrEmpty(functions)) {
            return ruleResult;
        }

        List<String> provisionedFunctions = Collections.synchronizedList(new ArrayList<>());
        functions.parallelStream().forEach(functionConfig -> {
                    List<ProvisionedConcurrencyConfigListItem> provisionConfigs =
                            lambdaAccessor.listProvisionedConcurrencyConfigs(functionConfig.functionName());
                    if (!provisionConfigs.isEmpty()) {
                        provisionedFunctions.add(functionConfig.functionName());
                    }
                }
        );

        if (!provisionedFunctions.isEmpty()) {
            Map<String, MetricDataResult> functionsNameToDataResultMap =
                    cloudWatchAccessor.getLambdaInvocationsMetricData(provisionedFunctions);
            log.debug("functionsNameToDataResultMap: {}", functionsNameToDataResultMap);
            for (Map.Entry<String, MetricDataResult> entry : functionsNameToDataResultMap.entrySet()) {
                if (entry.getValue().values().isEmpty()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            entry.getKey(), IssueCode.LAMBDA_FUNCTION_PROVISIONED_BUT_NOT_USED));
                }
            }
        }

        // https://docs.aws.amazon.com/lambda/latest/dg/foundation-arch.html
        for (FunctionConfiguration function : functions) {
            if (ARM64_SUPPORTED_RUNTIMES.contains(function.runtime())) {
                Optional<Architecture> arm64Arch =
                        function.architectures().stream().filter(arch -> arch == Architecture.ARM64).findAny();
                if (arm64Arch.isEmpty()) {
                    ruleResult.addIssueData(new IssueData(entityType,
                            function.functionName(), IssueCode.LAMBDA_FUNCTION_ARCH_CAN_BE_ARM64));
                } else if (NODEJS_RUNTIMES.contains(function.runtime())) {
                    Map<String, String> envVariables = function.environment() != null
                            ? function.environment().variables() : new HashMap<>();
                    if (!envVariables.containsKey(AWS_NODEJS_CONNECTION_REUSE_ENABLED)
                            || !"1".equals(envVariables.get(AWS_NODEJS_CONNECTION_REUSE_ENABLED))) {
                        ruleResult.addIssueData(new IssueData(entityType,
                                function.functionName(), IssueCode.AWS_NODEJS_CONNECTION_REUSE_ENABLED_MISSING));
                    }
                }
            }

            if (DEPRECATED_RUNTIMES.contains(function.runtime())) {
                ruleResult.addIssueData(new IssueData(entityType,
                        function.functionName(), IssueCode.LAMBDA_RUNTIME_DEPRECATED));
            }
        }

        ruleResult.addServiceData(new ServiceData(entityType, functions.stream()
                .map(func -> new ResourceMetadata(func.functionName(),
                        TimeUtils.convertToInstant(func.lastModified()))).collect(Collectors.toList())));

        return ruleResult;
    }
}
