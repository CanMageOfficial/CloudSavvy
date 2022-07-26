package com.cloudSavvy.dagger;

import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.run.RunStatistics;
import com.cloudSavvy.execution.GlobalAnalyzer;
import com.cloudSavvy.execution.GlobalExecutor;
import com.cloudSavvy.execution.NewIssueDetector;
import com.cloudSavvy.execution.RegionDiscovery;
import com.cloudSavvy.execution.ReportExecutor;
import com.cloudSavvy.frontend.RunScreen;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.sts.StsClient;

import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;

@Module
public class ExecutorModule {
    @Provides
    @Singleton
    public GlobalAnalyzer provideGlobalAnalyzer(final RegionDiscovery regionDiscovery,
                                                final ScheduledExecutorService scheduledExecutorService,
                                                final RunScreen runScreen,
                                                final RunStatistics runStatistics,
                                                final GlobalCache globalCache) {

        return new GlobalAnalyzer(regionDiscovery, scheduledExecutorService, runScreen, runStatistics, globalCache);
    }

    @Provides
    @Singleton
    public GlobalExecutor provideGlobalExecutor(final GlobalAnalyzer globalAnalyzer,
                                                final ReportExecutor reportExecutor,
                                                final NewIssueDetector newIssueDetector) {
        return new GlobalExecutor(globalAnalyzer, reportExecutor, newIssueDetector);
    }

    @Provides
    @Singleton
    public StsClient provideStsClient() {
        return StsClient.create();
    }
}
