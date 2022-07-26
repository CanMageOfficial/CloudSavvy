package com.cloudSavvy.commandline;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;

import java.util.List;

@Builder
@Getter
public class CommandLineData {
    private List<Region> regions;
}
