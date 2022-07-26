package com.cloudSavvy.utils;

public class EnvironmentUtils {

    private static final String LAMBDA_TASK_ROOT = "LAMBDA_TASK_ROOT";
    private static final String RESULTS_BUCKET = "RESULTS_BUCKET";
    private static final String AWS_REGION = "AWS_REGION";
    public static final String FROM_EMAIL_ADDRESS = "FROM_EMAIL_ADDRESS";
    public static final String TO_EMAIL_ADDRESSES = "TO_EMAIL_ADDRESSES";
    private static final String REPORT_ALL_RESOURCES = "REPORT_ALL_RESOURCES";
    private static final String REGIONS = "REGIONS";
    private static final String RUNNING_AWS_ACCOUNT_ID = "RUNNING_AWS_ACCOUNT_ID";
    private static final String STACK_NAME = "STACK_NAME";
    private static final String IGNORE_RESOURCE_AGE = "IGNORE_RESOURCE_AGE";

    private static final String IGNORE_AVAILABILITY = "IGNORE_AVAILABILITY";
    public static final String NOTIFICATION_TOPIC_ARN = "NOTIFICATION_TOPIC_ARN";
    public static final String IS_DEBUG = "IS_DEBUG";

    private static String getEnvVar(final String name) {
        String value = System.getenv(name);
        return value != null ? value.trim() : null;
    }

    public static boolean isRunningInLambda() {
        String taskRoot = getEnvVar(LAMBDA_TASK_ROOT);
        return taskRoot != null;
    }

    public static String getResultsBucket() {
        return getEnvVar(RESULTS_BUCKET);
    }

    public static String getFromEMailAddress() {
        return getEnvVar(FROM_EMAIL_ADDRESS);
    }

    public static String getToEmailAddresses() {
        return getEnvVar(TO_EMAIL_ADDRESSES);
    }

    public static String getLambdaRegion() {
        return getEnvVar(AWS_REGION);
    }

    public static boolean isReportAllResources() {
        String reportAllResourcesText = getEnvVar(REPORT_ALL_RESOURCES);
        if (reportAllResourcesText == null) {
            return false;
        }

        return Boolean.parseBoolean(reportAllResourcesText);
    }

    public static boolean isDebug() {
        String isDebugText = getEnvVar(IS_DEBUG);
        if (isDebugText == null) {
            return false;
        }

        return Boolean.parseBoolean(isDebugText);
    }

    public static String getRunRegions() {
        return getEnvVar(REGIONS);
    }

    public static String getAWSAccountId() {
        return getEnvVar(RUNNING_AWS_ACCOUNT_ID);
    }

    public static String getStackName() {
        return getEnvVar(STACK_NAME);
    }

    public static boolean ignoreResourceAge() {
        String ignoreResourceAgeText = getEnvVar(IGNORE_RESOURCE_AGE);
        return Boolean.parseBoolean(ignoreResourceAgeText);
    }

    public static boolean ignoreAvailability() {
        String ignoreAvailabilityText = getEnvVar(IGNORE_AVAILABILITY);
        return Boolean.parseBoolean(ignoreAvailabilityText);
    }

    public static String getNotificationTopicArn() {
        return getEnvVar(NOTIFICATION_TOPIC_ARN);
    }
}
