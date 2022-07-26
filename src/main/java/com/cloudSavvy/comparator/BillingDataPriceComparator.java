package com.cloudSavvy.comparator;

import com.cloudSavvy.aws.billing.DailyCharge;
import com.cloudSavvy.aws.billing.BillingData;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BillingDataPriceComparator implements Comparator<BillingData> {
    @Override
    public int compare(final BillingData o1, final BillingData o2) {
        double max1 = getMax(o1.getDailyCharges());
        double max2 = getMax(o2.getDailyCharges());
        return Double.compare(max1, max2);
    }

    public static Double getMax(List<DailyCharge> charges) {
        if (CollectionUtils.isNullOrEmpty(charges)) {
            return 0.0;
        }

        Optional<Double> max = charges.stream().map(DailyCharge::getValue).max(Double::compareTo);
        return max.orElse(0.0);
    }
}
