package com.cloudSavvy.dagger;

import com.cloudSavvy.common.run.RunStatistics;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { CommonModule.class, CredentialsModule.class })
public interface CommonComponent {
    RunStatistics getRunStatistics();
}
