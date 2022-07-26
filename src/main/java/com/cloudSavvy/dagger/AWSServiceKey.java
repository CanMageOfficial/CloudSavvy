package com.cloudSavvy.dagger;

import com.cloudSavvy.aws.common.AWSService;
import dagger.MapKey;

@MapKey
@interface AWSServiceKey {
    @SuppressWarnings("unused") AWSService value();
}
