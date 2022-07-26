package com.cloudSavvy.aws.cloudsearch;

import com.cloudSavvy.aws.rule.RuleResult;
import com.cloudSavvy.common.AnalyzerRule;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.common.ServiceData;
import com.cloudSavvy.utils.MetricUtils;
import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.aws.metric.AWSMetric;
import com.cloudSavvy.aws.metric.MetricConstants;
import com.cloudSavvy.aws.rule.RuleContext;
import com.cloudSavvy.common.ResourceMetadata;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.cloudsearch.model.DomainStatus;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CloudSearchDomainUsageRule implements AnalyzerRule {
    private CloudSearchAccessor cloudSearchAccessor;
    private CloudWatchAccessor cloudWatchAccessor;

    private final EntityType entityType = EntityType.CLOUDSEARCH_DOMAIN;

    @Override
    public AWSService getAWSService() {
        return AWSService.CloudSearch;
    }

    @Override
    public RuleResult call(RuleContext ruleContext) {
        RuleResult ruleResult = new RuleResult();
        List<DomainStatus> domainStatuses = cloudSearchAccessor.listDomainNames();
        if (CollectionUtils.isNullOrEmpty(domainStatuses)) {
            return ruleResult;
        }

        List<String> domainNames = domainStatuses.stream()
                .map(DomainStatus::domainName).collect(Collectors.toList());
        ruleResult.addServiceData(new ServiceData(entityType, domainNames.stream()
                .map(domainName -> new ResourceMetadata(domainName, null))
                .collect(Collectors.toList())));

        List<Metric> metrics = cloudWatchAccessor.listMetrics(
                Objects.requireNonNull(MetricConstants.AWSMetricTraits.get(AWSMetric.CLOUDSEARCH_SUCCESSFUL_REQUESTS)));
        Map<String, String> dimensionMap = getMetricDimension(metrics);
        Map<String, MetricDataResult> metricMap =
                cloudWatchAccessor.getCloudSearchSuccessfulRequestsMetricData(domainNames, dimensionMap);
        for (Map.Entry<String, MetricDataResult> entry : metricMap.entrySet()) {
            MetricDataResult dataResult = entry.getValue();
            if (dataResult.values() != null && dataResult.values().size() > ResourceAge.SEVEN_DAYS
                    && MetricUtils.getMax(dataResult) < 1) {
                ruleResult.addIssueData(new IssueData(entityType, entry.getKey(),
                        IssueCode.CLOUD_SEARCH_DOMAIN_NOT_USED));
            }
        }

        return ruleResult;
    }

    private Map<String, String> getMetricDimension(List<Metric> metrics) {
        Map<String, String> dimensionMap = new HashMap<>();
        for (Metric metric : metrics) {
            Optional<Dimension> domainNameOptional =
                    metric.dimensions().stream().filter(dimension -> "DomainName".equals(dimension.name())).findFirst();
            Optional<Dimension> clientIdOptional =
                    metric.dimensions().stream().filter(dimension -> "ClientId".equals(dimension.name())).findFirst();
            if (domainNameOptional.isPresent() && clientIdOptional.isPresent()) {
                String domainName = domainNameOptional.get().value();
                String clientId = clientIdOptional.get().value();
                if (dimensionMap.containsKey(domainName)) {
                    dimensionMap.put(domainName, dimensionMap.get(domainName).concat("|").concat(clientId));
                } else {
                    dimensionMap.put(domainName, clientId);
                }
            }
        }
        return dimensionMap;
    }
}
