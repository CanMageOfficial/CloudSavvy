package com.cloudSavvy.utils;

import com.google.common.collect.ImmutableSet;

public class CdkUtils {

    // CDK-internal Lambdas identified by stable substrings (stack-name prefix and trailing hash vary per account)
    private static final ImmutableSet<String> CDK_INTERNAL_LAMBDA_SUBSTRINGS = ImmutableSet.of(
            "BucketNotificationsHandler",
            "CustomCDKBucketDeployment",
            "CustomS3AutoDeleteObjects",
            "LogRetention"
    );

    public static boolean isCdkInternalLambda(String functionName) {
        for (String substring : CDK_INTERNAL_LAMBDA_SUBSTRINGS) {
            if (functionName.contains(substring)) {
                return true;
            }
        }
        return false;
    }
}