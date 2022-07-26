package com.cloudSavvy.reporting;

import lombok.Getter;

@Getter
public enum ReportType {
    ISSUE_DATA_HTML("Detected_Issues", ReportingConstants.HTML_EXTENSION),
    FULL_ISSUE_DATA_HTML("Detected_Issues", ReportingConstants.HTML_EXTENSION),
    SERVICE_DATA_HTML("Analyzed_Resources", ReportingConstants.HTML_EXTENSION),
    ERROR_DATA_HTML("CloudSavvy_Debug_Data", ReportingConstants.HTML_EXTENSION),
    SHORT_MESSAGE("Short_Message"),
    DAILY_CHARGES_HTML("Daily_Estimated_Charges", ReportingConstants.HTML_EXTENSION),
    INTERNAL_DATA_JSON("CloudSavvy_Internal_Data", ReportingConstants.JSON_EXTENSION);

    private final String text;
    private final String extension;

    ReportType(String text) {
        this.text = text;
        this.extension = null;
    }

    ReportType(String text, String extension) {

        this.text = text;
        this.extension = extension;
    }

    public String getFileName() {
        if (extension != null) {
            return text.concat(extension);
        } else {
            return text;
        }
    }
}
