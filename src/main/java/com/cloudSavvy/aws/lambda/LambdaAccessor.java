package com.cloudSavvy.aws.lambda;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.ListProvisionedConcurrencyConfigsRequest;
import software.amazon.awssdk.services.lambda.model.ListProvisionedConcurrencyConfigsResponse;
import software.amazon.awssdk.services.lambda.model.ProvisionedConcurrencyConfigListItem;
import software.amazon.awssdk.services.lambda.paginators.ListFunctionsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class LambdaAccessor {

    private LambdaClient lambdaClient;

    public List<FunctionConfiguration> listFunctions() {
        ListFunctionsIterable functionsIterable = lambdaClient.listFunctionsPaginator();
        List<FunctionConfiguration> functions = new ArrayList<>();

        for (FunctionConfiguration functionConfiguration : functionsIterable.functions()) {
            functions.add(functionConfiguration);

            if (functions.size() > 1000) {
                break;
            }
        }
        log.debug("Found {} functions", functions.size());
        return functions;
    }

    public List<ProvisionedConcurrencyConfigListItem> listProvisionedConcurrencyConfigs(String functionName) {
        ListProvisionedConcurrencyConfigsRequest request =
                ListProvisionedConcurrencyConfigsRequest.builder().functionName(functionName).build();
        ListProvisionedConcurrencyConfigsResponse provisionResponse = lambdaClient.listProvisionedConcurrencyConfigs(request);

        List<ProvisionedConcurrencyConfigListItem> provisions = provisionResponse.provisionedConcurrencyConfigs();
        log.debug("Found {} provisions for function {}", provisions.size(), functionName);
        return provisions;
    }
}
