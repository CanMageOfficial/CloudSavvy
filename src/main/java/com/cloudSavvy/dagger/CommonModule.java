package com.cloudSavvy.dagger;

import com.cloudSavvy.execution.RegionDiscovery;
import com.cloudSavvy.frontend.RunScreen;
import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.run.RunStatistics;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class CommonModule {

    private static final String US_EAST_1_EC2_CLIENT = "US_EAST_1_EC2_CLIENT";

    @Provides
    @Singleton
    public ScheduledExecutorService provideScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Provides
    @Singleton
    public RunStatistics provideRunStatistics() {
        return new RunStatistics();
    }

    @Provides
    @Singleton
    public RunScreen provideRunScreen(RunStatistics runStatistics) {
        return new RunScreen(runStatistics);
    }

    @Provides
    @Singleton
    public GlobalCache provideServiceDataCache() {
        return new GlobalCache();
    }

    @Provides
    @Singleton
    @Named(US_EAST_1_EC2_CLIENT)
    public Ec2Client provideEc2Client(AwsCredentialsProvider awsCredentialsProvider) {
        return Ec2Client.builder().credentialsProvider(awsCredentialsProvider)
                .region(Region.US_EAST_1).build();
    }

    @Provides
    @Singleton
    public RegionDiscovery provideRegionDiscovery(@Named(US_EAST_1_EC2_CLIENT) Ec2Client ec2Client) {
        return new RegionDiscovery(ec2Client);
    }
}
