package com.cloudSavvy.utils;

import com.cloudSavvy.common.RegionAnalyzeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LoggingUtils {
    public static void printLongestRunningServices(List<RegionAnalyzeResult> results) {
        if (EnvironmentUtils.isRunningInLambda()) {
            log.info("Longest running services: {}{}", System.lineSeparator(),
                    RuntimeUtils.printLongestRunningService(results));
        }
    }
}
