package com.cloudSavvy.common;

import com.cloudSavvy.aws.billing.BillingData;
import com.cloudSavvy.aws.common.AWSService;
import com.cloudSavvy.aws.common.EntityType;
import com.cloudSavvy.aws.rule.RuleResult;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RegionAnalyzeResult {
    private final List<IssueData> issueDataList;
    private final Region region;

    private final List<ErrorData> errorDataList;

    private final Map<EntityType, ServiceData> serviceDataMap;
    private final Map<AWSService, Long> serviceRuntime;
    private final List<BillingData> billingDataList;
    private final Set<IssueData> newIssues;

    public RegionAnalyzeResult(Region region) {
        issueDataList = Collections.synchronizedList(new ArrayList<>());
        serviceDataMap = new ConcurrentHashMap<>();
        errorDataList = Collections.synchronizedList(new ArrayList<>());
        this.region = region;
        serviceRuntime = new ConcurrentHashMap<>();
        billingDataList = Collections.synchronizedList(new ArrayList<>());
        newIssues = Collections.synchronizedSet(new HashSet<>());
    }

    public synchronized void merge(RuleResult ruleResult) {
        issueDataList.addAll(ruleResult.getIssueDataList());
        for (ServiceData serviceData : ruleResult.getServiceDataList()) {
            addServiceData(serviceData);
        }
        billingDataList.addAll(ruleResult.getBillingDataList());
    }

    public synchronized void merge(RegionAnalyzeResult regionAnalyzeResult) {
        issueDataList.addAll(regionAnalyzeResult.getIssueDataList());
        for (Map.Entry<EntityType, ServiceData> serviceDataEntry : regionAnalyzeResult.serviceDataMap.entrySet()) {
            addServiceData(serviceDataEntry.getValue());
        }

        for (Map.Entry<AWSService, Long> runtimeEntry : regionAnalyzeResult.getServiceRuntime().entrySet()) {
            addServiceExecutionTime(runtimeEntry.getKey(), runtimeEntry.getValue());
        }

        errorDataList.addAll(regionAnalyzeResult.getErrorDataList());
        billingDataList.addAll(regionAnalyzeResult.getBillingDataList());
    }

    private void addServiceData(ServiceData serviceData) {
        if (CollectionUtils.isNullOrEmpty(serviceData.getResources())) {
            return;
        }

        if (serviceDataMap.containsKey(serviceData.getEntityType())) {
            serviceDataMap.get(serviceData.getEntityType())
                    .getResources().addAll(serviceData.getResources());
        } else {
            serviceDataMap.put(serviceData.getEntityType(), serviceData);
        }
    }

    public synchronized void addErrorData(ErrorData errorData) {
        errorDataList.add(errorData);
    }

    public synchronized void addServiceExecutionTime(AWSService service, long runtime) {
        serviceRuntime.put(service, runtime);
    }

    public synchronized void addNewIssue(IssueData issueData) {
        newIssues.add(issueData);
    }

    public String toString() {
        if (CollectionUtils.isNullOrEmpty(issueDataList)) {
            return "";
        }
        return String.format("Region: %s, issue data: %s", region, issueDataList);
    }
}
