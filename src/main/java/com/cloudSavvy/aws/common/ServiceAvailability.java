package com.cloudSavvy.aws.common;

import com.cloudSavvy.utils.EnvironmentUtils;
import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.regions.Region;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ServiceAvailability {

    // If service does not exist in AVAILABLE_REGIONS, it means it is available in all regions
    public static final ImmutableMap<AWSService, Set<Region>> AVAILABLE_REGIONS = ImmutableMap.<AWSService, Set<Region>>builder()
            .put(AWSService.CloudFront, new HashSet<>(Collections.singletonList(Region.US_EAST_1))).build();

    private static final ImmutableMap<AWSService, Set<Region>> UNAVAILABLE_REGIONS = ImmutableMap.<AWSService, Set<Region>>builder()
            .put(AWSService.Amazon_EventBridge, new HashSet<>(Arrays.asList(Region.AP_NORTHEAST_3,
                    Region.EU_CENTRAL_2, Region.ME_CENTRAL_1, Region.AP_SOUTH_2,
                    Region.EU_SOUTH_2)))
            .put(AWSService.Amazon_Redshift, new HashSet<>(Arrays.asList(Region.AP_NORTHEAST_3,
                    Region.SA_EAST_1, Region.AP_SOUTHEAST_3,
                    Region.AP_EAST_1, Region.ME_SOUTH_1, Region.AF_SOUTH_1, Region.EU_SOUTH_1, Region.EU_CENTRAL_2,
                    Region.ME_CENTRAL_1, Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.AppStream_2, new HashSet<>(Arrays.asList(Region.US_WEST_1, Region.AP_NORTHEAST_3,
                    Region.EU_WEST_3, Region.EU_NORTH_1, Region.AP_SOUTHEAST_3, Region.AP_EAST_1,
                    Region.ME_SOUTH_1, Region.AF_SOUTH_1, Region.EU_SOUTH_1, Region.EU_CENTRAL_2, Region.ME_CENTRAL_1,
                    Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.Lightsail, new HashSet<>(Arrays.asList(Region.US_WEST_1, Region.AP_NORTHEAST_3,
                    Region.SA_EAST_1, Region.AP_SOUTHEAST_3, Region.AP_EAST_1, Region.ME_SOUTH_1, Region.AF_SOUTH_1,
                    Region.EU_SOUTH_1, Region.EU_CENTRAL_2, Region.ME_CENTRAL_1, Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.CloudSearch, new HashSet<>(Arrays.asList(Region.EU_WEST_3, Region.AP_SOUTH_1,
                    Region.AP_NORTHEAST_3, Region.EU_NORTH_1, Region.EU_WEST_2, Region.US_EAST_2, Region.CA_CENTRAL_1,
                    Region.AP_SOUTHEAST_3, Region.AP_EAST_1, Region.ME_SOUTH_1, Region.AF_SOUTH_1, Region.EU_SOUTH_1,
                    Region.EU_CENTRAL_2, Region.ME_CENTRAL_1, Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.AWS_Transfer, new HashSet<>(Arrays.asList(Region.EU_CENTRAL_2,
                    Region.ME_CENTRAL_1, Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.RDS, new HashSet<>(Arrays.asList(Region.AP_SOUTHEAST_3, Region.EU_CENTRAL_2,
                    Region.ME_CENTRAL_1, Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.MEMORY_DB, new HashSet<>(Arrays.asList(Region.ME_SOUTH_1, Region.AP_SOUTHEAST_3,
                    Region.AP_NORTHEAST_3, Region.AF_SOUTH_1, Region.EU_CENTRAL_2, Region.ME_CENTRAL_1,
                    Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.GLUE, new HashSet<>(Arrays.asList(Region.ME_CENTRAL_1, Region.EU_CENTRAL_2,
                    Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.FSx, new HashSet<>(Arrays.asList(Region.EU_CENTRAL_2, Region.AP_SOUTH_2,
                    Region.EU_SOUTH_2)))
            .put(AWSService.Kinesis, new HashSet<>(Arrays.asList(Region.EU_CENTRAL_2, Region.ME_CENTRAL_1,
                    Region.AP_SOUTH_2, Region.EU_SOUTH_2)))
            .put(AWSService.EKS, new HashSet<>(Arrays.asList(Region.EU_CENTRAL_2, Region.AP_SOUTH_2,
                    Region.EU_SOUTH_2)))
            .put(AWSService.Amazon_SageMaker, new HashSet<>(Arrays.asList(Region.EU_CENTRAL_2, Region.AP_SOUTH_2,
                    Region.EU_SOUTH_2)))
            .put(AWSService.EFS, new HashSet<>(Collections.singletonList(Region.AP_SOUTH_2)))
            .build();

    public static final boolean IGNORE_AVAILABILITY = EnvironmentUtils.ignoreAvailability();

    public static boolean isAvailable(final AWSService service, final Region region) {
        if (UNAVAILABLE_REGIONS.containsKey(service) && Objects.requireNonNull(UNAVAILABLE_REGIONS.get(service)).contains(region)) {
            return false;
        }

        // If service does not exist, it means it is available in all regions
        if (!AVAILABLE_REGIONS.containsKey(service)) {
            return true;
        }

        return Objects.requireNonNull(AVAILABLE_REGIONS.get(service)).contains(region);
    }
}
