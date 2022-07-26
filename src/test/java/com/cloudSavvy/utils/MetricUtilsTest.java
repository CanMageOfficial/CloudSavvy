package com.cloudSavvy.utils;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricUtilsTest {

    @Test
    public void test_getMax_emptyValues() {
        MetricDataResult dataResult = MetricDataResult.builder()
                .values(new ArrayList<>()).build();
        assertEquals(0.0, MetricUtils.getMax(dataResult));
    }

    @Test
    public void test_getMax_empty() {
        Collection<Double> values = Arrays.asList(2.1, 5.2, 3.0);
        MetricDataResult dataResult = MetricDataResult.builder()
                .values(values).build();
        assertEquals(5.2, MetricUtils.getMax(dataResult));
    }
}
