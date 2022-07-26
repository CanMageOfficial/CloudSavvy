package com.cloudSavvy.aws.rule;

import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.aws.billing.BillingData;
import com.cloudSavvy.common.ServiceData;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RuleResult {
    private final List<IssueData> issueDataList;
    private final List<ServiceData> serviceDataList;
    private final List<BillingData> billingDataList;

    public RuleResult() {
        issueDataList = new ArrayList<>();
        serviceDataList = new ArrayList<>();
        billingDataList = new ArrayList<>();
    }

    public synchronized void addServiceData(ServiceData serviceData) {
        serviceDataList.add(serviceData);
    }

    public synchronized void addIssueData(IssueData issueData) {
        issueDataList.add(issueData);
    }

    public synchronized void addBillingData(BillingData billingData) {
        billingDataList.add(billingData);
    }
}
