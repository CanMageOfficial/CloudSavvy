package com.cloudSavvy.aws.common;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceAvailabilityTest {

    @Test
    public void isAvailable() {
        boolean result = ServiceAvailability.isAvailable(AWSService.Amazon_Redshift, Region.US_EAST_1);
        assertTrue(result);
        result = ServiceAvailability.isAvailable(AWSService.Amazon_Redshift, Region.AP_NORTHEAST_3);
        assertFalse(result);

        result = ServiceAvailability.isAvailable(AWSService.CloudFront, Region.AF_SOUTH_1);
        assertFalse(result);
        result = ServiceAvailability.isAvailable(AWSService.CloudFront, Region.US_EAST_1);
        assertTrue(result);

        result = ServiceAvailability.isAvailable(AWSService.Amazon_EventBridge, Region.US_EAST_1);
        assertTrue(result);
        result = ServiceAvailability.isAvailable(AWSService.Amazon_EventBridge, Region.AP_NORTHEAST_3);
        assertFalse(result);
        result = ServiceAvailability.isAvailable(AWSService.Amazon_EventBridge, Region.AP_NORTHEAST_2);
        assertTrue(result);
    }
}
