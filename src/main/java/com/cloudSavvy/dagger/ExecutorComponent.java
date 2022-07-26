package com.cloudSavvy.dagger;

import com.cloudSavvy.execution.GlobalExecutor;
import dagger.Component;
import software.amazon.awssdk.services.sts.StsClient;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CommonModule.class, CredentialsModule.class, ExecutorModule.class, ReportingModule.class})
public interface ExecutorComponent {
    GlobalExecutor getGlobalExecutor();

    StsClient getStsClient();
}
