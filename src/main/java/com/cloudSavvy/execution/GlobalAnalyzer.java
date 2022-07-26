package com.cloudSavvy.execution;

import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.common.run.RunStatistics;
import com.cloudSavvy.frontend.RunScreen;
import com.cloudSavvy.utils.LoggingUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class GlobalAnalyzer {

    private final RegionDiscovery regionDiscovery;
    private final ScheduledExecutorService scheduledExecutorService;
    private final RunScreen runScreen;
    private final RunStatistics runStatistics;
    private final GlobalCache globalCache;

    public List<Region> findRegionsToAnalyze(final ExecutionInput executionInput) {
        List<Region> regions = executionInput.getRequestedRegions();
        if (CollectionUtils.isNullOrEmpty(regions)) {
            regions = regionDiscovery.getRegions();
        }

        log.info("Analyzing these regions: {}", regions);

        return regions;
    }

    public List<RegionAnalyzeResult> analyzeRegions(List<Region> regions) {
        List<RegionAnalyzeResult> regionResults = Collections.synchronizedList(new ArrayList<>());

        List<RegionalExecutor> regionalExecutors = new ArrayList<>();
        try {
            int regionsSize = regions.size();
            regions.forEach(region -> {
                RunMetadata runMetadata = RunMetadata.builder().region(region)
                        .numberOfRegionsAnalyzed(regionsSize).build();

                // CommonComponent needs to be passed because it provides singleton objects for all regions
                // Also we need to create new RegionalExecutor because running regions are dynamic.
                // We cannot use dagger to create objects
                regionalExecutors.add(new RegionalExecutor(runStatistics, globalCache, runMetadata));
            });

            scheduledExecutorService.scheduleAtFixedRate(runScreen, 2, 10, TimeUnit.SECONDS);

            regionalExecutors.parallelStream().forEach(regionalExecutor ->
                    regionResults.add(regionalExecutor.call()));
        } finally {
            scheduledExecutorService.shutdown();
        }

        LoggingUtils.printLongestRunningServices(regionResults);

        return regionResults;
    }
}
