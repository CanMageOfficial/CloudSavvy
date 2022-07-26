package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.billing.DailyCharge;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.run.RunContext;
import com.cloudSavvy.aws.billing.BillingConstants;
import com.cloudSavvy.aws.billing.BillingData;
import com.cloudSavvy.aws.billing.EstimatedChargesMetric;
import com.cloudSavvy.aws.common.AWSService;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CloudWatchBillingRule implements AnalyzerRule {

    private CloudWatchAccessor cloudWatchAccessor;
    private RunContext runContext;

    @Override
    public AWSService getAWSService() {
        return AWSService.CloudWatch;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        final RuleResult ruleResult = new RuleResult();

        if (runContext.getRunMetadata().getRegion() != Region.US_EAST_1) {
            return ruleResult;
        }

        EstimatedChargesMetric estimatedChargesMetric = getEstimatedChargesMetric();
        List<String> serviceNames = estimatedChargesMetric.getServiceNames();
        String currency = estimatedChargesMetric.getCurrency();

        if (currency == null || CollectionUtils.isNullOrEmpty(serviceNames)) {
            return ruleResult;
        }

        Map<String, String> extraDimensions = Map.of(BillingConstants.CURRENCY, currency);
        serviceNames.add(BillingConstants.ALL_SERVICES);
        Map<String, MetricDataResult> metricDataResultMap =
                cloudWatchAccessor.getEstimatedChargesMetricData(serviceNames, extraDimensions);

        for (Map.Entry<String, MetricDataResult> entry : metricDataResultMap.entrySet()) {
            MetricDataResult metricDataResult = entry.getValue();
            String serviceName = entry.getKey();
            if (!areAllZeroes(metricDataResult)
                    && metricDataResult.values().size() == metricDataResult.timestamps().size()) {

                checkPriceJump(serviceName, metricDataResult, ruleResult);
                BillingData billingData = new BillingData();
                billingData.setServiceName(serviceName);
                billingData.setCurrency(currency);
                for (int i = 0; i < metricDataResult.values().size(); i++) {
                    billingData.getDailyCharges().add(DailyCharge.builder()
                            .date(metricDataResult.timestamps().get(i))
                            .value(metricDataResult.values().get(i)).build());
                }
                ruleResult.addBillingData(billingData);
            }
        }

        return ruleResult;
    }

    private boolean areAllZeroes(MetricDataResult metricDataResult) {
        return metricDataResult.values().stream().noneMatch(value -> value > 0.0);
    }

    public EstimatedChargesMetric getEstimatedChargesMetric() {
        List<Metric> metrics = cloudWatchAccessor.listEstimatedChargesMetrics();
        List<String> serviceNames = new ArrayList<>(metrics.size());
        String currency = null;

        for (Metric metric : metrics) {
            for (Dimension dimension : metric.dimensions()) {
                if (dimension.name().equals(BillingConstants.SERVICE_NAME)) {
                    serviceNames.add(dimension.value());
                } else if (dimension.name().equals(BillingConstants.CURRENCY)) {
                    currency = dimension.value();
                }
            }
        }
        return EstimatedChargesMetric.builder()
                .currency(currency).serviceNames(serviceNames).build();
    }

    public void checkPriceJump(String serviceName, MetricDataResult dataResult, RuleResult ruleResult) {
        Double previousAmount = 0.0;
        for (int i = dataResult.values().size() - 1; i >= 0 ; i--) {
            Double amount = dataResult.values().get(i);

            if (previousAmount >= 1.0 &&  amount >= previousAmount * 10) {
                ruleResult.addIssueData(new IssueData(EntityType.BILLING,
                        serviceName, IssueCode.SERVICE_COST_HAS_SPIKE));
            }

            previousAmount = amount;
        }
    }
}
