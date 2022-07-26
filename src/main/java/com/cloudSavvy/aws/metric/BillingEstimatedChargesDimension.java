package com.cloudSavvy.aws.metric;

import com.cloudSavvy.aws.billing.BillingConstants;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BillingEstimatedChargesDimension implements MetricDimensionBuilder {
    @Override
    public Collection<Dimension> buildDimensions(String name, String value, Map<String, String> extraDimensions) {
        ArrayList<Dimension> dimensions = new ArrayList<>(2);
        if (value != null && !BillingConstants.ALL_SERVICES.equals(value)) {
            dimensions.add(Dimension.builder().name(name).value(value).build());
        }

        String currency = extraDimensions.get(BillingConstants.CURRENCY);
        dimensions.add(Dimension.builder().name(BillingConstants.CURRENCY).value(currency).build());

        return dimensions;
    }
}
