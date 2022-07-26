package com.cloudSavvy.dagger;

import com.cloudSavvy.execution.AWSAnalyzer;
import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.common.run.RunStatistics;
import dagger.BindsInstance;
import dagger.Component;

import java.util.Set;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AWSAnalyzerModule.class,
        AWSClientModule.class, AWSAccessorModule.class, CredentialsModule.class})
public interface AWSConnectorComponent {

    Set<AWSAnalyzer> getAnalyzers();

    @Component.Factory
    interface Factory {

        AWSConnectorComponent create(@BindsInstance RunMetadata runMetadata,
                                     @BindsInstance RunStatistics runStatistics,
                                     @BindsInstance GlobalCache serviceDataCache);
    }
}
