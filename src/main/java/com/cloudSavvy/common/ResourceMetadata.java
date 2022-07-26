package com.cloudSavvy.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@ToString
@Data
public class ResourceMetadata {
    private String resourceId;

    // this is creation date, if creation date is not available last modified date will be used.
    private Instant activityDate;
}
