package com.cloudSavvy.utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegionUtils {
    private static final Map<String, Region> regions =
            Region.regions().stream().collect(Collectors.toMap(Region::id, Function.identity()));

    public static Region parseRegion(String regionName) {
        if (!regions.containsKey(regionName)) {
            String message =
                    String.format("Unknown region is provided:%s. Sample format=us-east-1,us-east-2,us-west-1", regionName);
            throw new IllegalArgumentException(message);
        }

        Region region = regions.get(regionName);
        if (region.isGlobalRegion()) {
            throw new IllegalArgumentException("Global region is not supported: " + regionName);
        }

        return region;
    }

    public static List<Region> parseInputRegions(String regionList) {
        if (StringUtils.isEmpty(regionList)) {
            return null;
        }

        return Arrays.stream(regionList.split(","))
                .map(RegionUtils::parseRegion).collect(Collectors.toList());
    }
}
