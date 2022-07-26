package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.metric.BillingEstimatedChargesDimension;
import com.cloudSavvy.aws.metric.CloudSearchClientDimension;
import com.cloudSavvy.aws.metric.DefaultMetricDimension;
import com.cloudSavvy.aws.metric.ElastiCacheShardMetricDimension;
import com.cloudSavvy.aws.metric.MetricConstants;
import com.cloudSavvy.aws.metric.MetricDimensionBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CloudWatchAccessorTest {
    @Mock
    private CloudWatchClient cloudWatchClient;

    @Captor
    ArgumentCaptor<GetMetricDataRequest> metricDataRequestCaptor;

    @Test
    public void test_getLambdaInvocationsMetricData() {
        CloudWatchAccessor cloudWatchAccessor = getCloudWatchAccessor();
        List<String> functionNames = Arrays.asList("func1", "func2", "func3");
        Collection<MetricDataResult> metricDataResults = buildMetricDataResults(cloudWatchAccessor, functionNames);
        GetMetricDataResponse getMetricDataResponse = GetMetricDataResponse.builder()
                .metricDataResults(metricDataResults).build();
        Mockito.when(cloudWatchClient.getMetricData(metricDataRequestCaptor.capture())).thenReturn(getMetricDataResponse);

        Map<String, MetricDataResult> result = cloudWatchAccessor.getLambdaInvocationsMetricData(functionNames);
        Set<String> functionNamesSet = new HashSet<>(functionNames);
        assertEquals(3, result.size());
        for (Map.Entry<String, MetricDataResult> entry : result.entrySet()) {
            assertTrue(functionNamesSet.contains(entry.getKey()));
        }

        GetMetricDataRequest metricDataRequest = metricDataRequestCaptor.getValue();
        List<MetricDataQuery> metricDataQueries = metricDataRequest.metricDataQueries();
        assertEquals(3, metricDataQueries.size());
        assertEquals(MetricConstants.LAMBDA_NAMESPACE, metricDataQueries.get(0).metricStat().metric().namespace());
        assertEquals(MetricConstants.LAMBDA_INVOCATIONS, metricDataQueries.get(0).metricStat().metric().metricName());
    }

    private CloudWatchAccessor getCloudWatchAccessor() {
        DefaultMetricDimension defaultDimensionBuilder = new DefaultMetricDimension();
        ElastiCacheShardMetricDimension shardMetricDimension = new ElastiCacheShardMetricDimension();
        CloudSearchClientDimension clientDimension = new CloudSearchClientDimension();
        BillingEstimatedChargesDimension billingDimension = new BillingEstimatedChargesDimension();
        MetricDimensionBuilderFactory dimensionBuilderFactory = new MetricDimensionBuilderFactory(
                defaultDimensionBuilder, shardMetricDimension, clientDimension,billingDimension);
        return new CloudWatchAccessor(cloudWatchClient, dimensionBuilderFactory);
    }

    private Collection<MetricDataResult> buildMetricDataResults(CloudWatchAccessor cloudWatchAccessor, List<String> functionNames) {
        Collection<MetricDataResult> metricDataResults = new ArrayList<>();
        for (String functionName : functionNames) {
            metricDataResults.add(MetricDataResult.builder()
                    .id(cloudWatchAccessor.generateMetricId(functionName)).build());
        }
        return metricDataResults;
    }
}
