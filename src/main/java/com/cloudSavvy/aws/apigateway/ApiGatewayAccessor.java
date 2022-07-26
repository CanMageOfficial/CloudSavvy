package com.cloudSavvy.aws.apigateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.GetResourcesRequest;
import software.amazon.awssdk.services.apigateway.model.Resource;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.services.apigateway.paginators.GetResourcesIterable;
import software.amazon.awssdk.services.apigateway.paginators.GetRestApisIterable;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class ApiGatewayAccessor {
    private ApiGatewayV2Client apiGatewayV2Client;
    private ApiGatewayClient apiGatewayClient;

    public List<Api> listV2Apis() {
        String token = null;
        List<Api> apis = new ArrayList<>();
        do {
            GetApisRequest request = GetApisRequest.builder().nextToken(token).build();
            GetApisResponse apisResponse = apiGatewayV2Client.getApis(request);
            apis.addAll(apisResponse.items());
            token = apisResponse.nextToken();
        } while (token != null && apis.size() < 1000);
        return apis;
    }

    public List<RestApi> listRestApis() {
        List<RestApi> apis = new ArrayList<>();
        GetRestApisIterable apisResponse = apiGatewayClient.getRestApisPaginator();
        for (RestApi restApi : apisResponse.items()) {
            apis.add(restApi);

            if (apis.size() > 1000) {
                break;
            }
        }
        return apis;
    }

    public List<Resource> listResources(final String restApiId) {
        List<Resource> resources = new ArrayList<>();
        GetResourcesRequest request = GetResourcesRequest.builder().embed("methods").restApiId(restApiId).build();
        GetResourcesIterable apisResponse = apiGatewayClient.getResourcesPaginator(request);
        for (Resource resource : apisResponse.items()) {
            resources.add(resource);

            if (resources.size() > 1000) {
                break;
            }
        }
        return resources;
    }
}
