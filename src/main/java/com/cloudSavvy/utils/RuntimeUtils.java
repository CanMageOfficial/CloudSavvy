package com.cloudSavvy.utils;

import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.aws.common.AWSService;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.Map;

public class RuntimeUtils {
    public static String printLongestRunningService(List<RegionAnalyzeResult> results) {
        long max;
        Region region;
        AWSService awsService;
        StringBuilder sb = new StringBuilder();
        for (RegionAnalyzeResult result : results) {
            max = -1;
            region = null;
            awsService = null;
            for (Map.Entry<AWSService, Long> entrySet : result.getServiceRuntime().entrySet()) {
                if (entrySet.getValue() > max) {
                    max = entrySet.getValue();
                    region = result.getRegion();
                    awsService = entrySet.getKey();
                }
            }
            if (region != null) {
                sb.append(String.format("Region: %s, service: %s, runtime: %s", region, awsService, max));
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
