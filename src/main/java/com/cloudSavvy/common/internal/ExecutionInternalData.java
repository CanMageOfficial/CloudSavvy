package com.cloudSavvy.common.internal;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExecutionInternalData {
    List<ExRegionRunResult> regionRunResults;

    public ExecutionInternalData() {
        regionRunResults = new ArrayList<>();
    }
}
