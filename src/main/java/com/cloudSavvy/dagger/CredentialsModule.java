package com.cloudSavvy.dagger;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import javax.inject.Singleton;

@Module
public class CredentialsModule {
    @Provides
    @Singleton
    public AwsCredentialsProvider provideCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }
}
