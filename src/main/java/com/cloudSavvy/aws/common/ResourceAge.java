package com.cloudSavvy.aws.common;

import com.cloudSavvy.utils.EnvironmentUtils;

public class ResourceAge {
    public static final boolean ignoreResourceAge = EnvironmentUtils.ignoreResourceAge();
    public static final int SEVEN_DAYS = initDays(7);
    public static final int THIRTY_DAYS = initDays(30);
    public static final int SIX_MONTHS = initDays(180);

    public static int initDays(int defaultValue) {
        if (ignoreResourceAge) {
            return -1;
        }
        return defaultValue;
    }
}
