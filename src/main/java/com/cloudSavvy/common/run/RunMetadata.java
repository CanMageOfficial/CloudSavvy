package com.cloudSavvy.common.run;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.regions.Region;

@Builder
@Data
public class RunMetadata {
    private Region region;
    private int numberOfRegionsAnalyzed;
}
