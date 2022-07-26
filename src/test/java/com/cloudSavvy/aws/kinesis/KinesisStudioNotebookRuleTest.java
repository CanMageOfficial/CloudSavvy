package com.cloudSavvy.aws.kinesis;

import com.cloudSavvy.aws.cloudwatch.CloudWatchAccessor;
import com.cloudSavvy.aws.metric.MetricConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class KinesisStudioNotebookRuleTest {

    @Mock
    private KinesisAccessor kinesisAccessor;

    @Mock
    private CloudWatchAccessor cloudWatchAccessor;

    @Test
    public void test_isRunningLong_NoData() {
        KinesisStudioNotebookRule rule = new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor);
        List<Double> metricValues = new ArrayList<>();
        boolean isLongRunning = rule.isRunningLong(metricValues);
        assertFalse(isLongRunning);
    }

    @Test
    public void test_isRunningLong_7DaysData() {
        KinesisStudioNotebookRule rule = new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor);
        List<Double> metricValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            metricValues.add((double) MetricConstants.MILLISECONDS_IN_20_HOURS);
        }
        metricValues.add(1.0);
        boolean isLongRunning = rule.isRunningLong(metricValues);
        assertFalse(isLongRunning);
    }

    @Test
    public void test_isRunningLong_8DaysData() {
        KinesisStudioNotebookRule rule = new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor);
        List<Double> metricValues = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            metricValues.add((double) MetricConstants.MILLISECONDS_IN_20_HOURS);
        }
        metricValues.add(1.0);
        boolean isLongRunning = rule.isRunningLong(metricValues);
        assertTrue(isLongRunning);
    }

    @Test
    public void test_isRunningLong_9DaysData() {
        KinesisStudioNotebookRule rule = new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor);
        List<Double> metricValues = new ArrayList<>();
        metricValues.add(1.0);
        for (int i = 0; i < 7; i++) {
            metricValues.add((double) MetricConstants.MILLISECONDS_IN_20_HOURS);
        }
        metricValues.add(1.0);
        boolean isLongRunning = rule.isRunningLong(metricValues);
        assertTrue(isLongRunning);
    }

    @Test
    public void test_isRunningLong_1DayData() {
        KinesisStudioNotebookRule rule = new KinesisStudioNotebookRule(kinesisAccessor, cloudWatchAccessor);
        List<Double> metricValues = new ArrayList<>();
        metricValues.add(1.0);
        boolean isLongRunning = rule.isRunningLong(metricValues);
        assertFalse(isLongRunning);
    }
}
