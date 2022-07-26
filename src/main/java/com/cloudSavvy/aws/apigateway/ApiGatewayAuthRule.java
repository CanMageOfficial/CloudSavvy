package com.cloudSavvy.aws.apigateway;

import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ResourceMetadata;
import com.cloudSavvy.common.ServiceData;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.apigateway.model.Method;
import software.amazon.awssdk.services.apigateway.model.Resource;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class ApiGatewayAuthRule implements AnalyzerRule {

    private ApiGatewayAccessor apiGatewayAccessor;

    private final EntityType methodEntityType = EntityType.APIGateway_METHOD;
    private final EntityType apiEntityType = EntityType.APIGateway_API;

    private static final ImmutableSet<String> AUTH_ENFORCED_METHODS = ImmutableSet.<String>builder()
            .add("POST").add("DELETE").add("PATCH").add("PUT").add("ANY").build();

    @Override
    public AWSService getAWSService() {
        return AWSService.API_GATEWAY;
    }

    @Override
    public RuleResult call(final RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<RestApi> apis = apiGatewayAccessor.listRestApis();

        if (CollectionUtils.isNullOrEmpty(apis)) {
            return ruleResult;
        }

        ruleResult.addServiceData(new ServiceData(apiEntityType, apis.stream()
                .map(api -> new ResourceMetadata(api.id(), api.createdDate())).collect(Collectors.toList())));

        apis.stream().parallel().forEach(api -> {
            List<Resource> resources = apiGatewayAccessor.listResources(api.id());
            if (!CollectionUtils.isNullOrEmpty(resources)) {
                for (Resource resource : resources) {
                    for (Map.Entry<String, Method> entry : resource.resourceMethods().entrySet()) {
                        if (AUTH_ENFORCED_METHODS.contains(entry.getKey())) {
                            Method method = entry.getValue();
                            if (!Boolean.TRUE.equals(method.apiKeyRequired())
                                    && (method.authorizationType() == null || method.authorizationType().equals("NONE"))) {
                                String entityAddress =
                                        api.id() + "/resources/" + resource.id() + "/methods/" + entry.getKey();
                                ruleResult.addIssueData(new IssueData(methodEntityType, entityAddress,
                                        IssueCode.API_GATEWAY_API_METHOD_HAS_NO_SECURITY));
                            }
                        }
                    }
                }
            }
        });

        return ruleResult;
    }
}
