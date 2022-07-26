package com.cloudSavvy.aws.common;

public enum IssueSeverity {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High");

    private final int severity;
    private final String text;

    IssueSeverity(int sev, String text) {
        this.severity = sev;
        this.text = text;
    }

    public int getValue() {
        return severity;
    }

    public String toString() {
        return text;
    }
}
