package com.cloudSavvy.aws.billing;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BillingData {
    private String serviceName;
    private List<DailyCharge> dailyCharges;
    private String currency;

    public BillingData() {
        dailyCharges = new ArrayList<>();
    }
}
