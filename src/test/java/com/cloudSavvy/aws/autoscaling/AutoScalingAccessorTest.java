package com.cloudSavvy.aws.autoscaling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AutoScalingAccessorTest {

    @Mock
    private ApplicationAutoScalingClient autoScalingClient;

    @Test
    public void test_parseResourceId() {

        AutoScalingAccessor accessor = new AutoScalingAccessor(autoScalingClient);
        assertEquals("test", accessor.parseResourceId("table/test"));
    }
}
