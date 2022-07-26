package com.cloudSavvy.utils;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RegionUtilsTest {
    @Test
    public void test_parseRegion() {
        Region region = RegionUtils.parseRegion("us-east-1");
        assertEquals(Region.US_EAST_1, region);

        assertThrows(IllegalArgumentException.class, () -> RegionUtils.parseRegion("invalid"));
        assertThrows(IllegalArgumentException.class, () -> RegionUtils.parseRegion("aws-iso-global"));
    }
}
