package com.cloudSavvy.commandline;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.*;

public class CSCommandLineParserTest {
    @Test
    public void test_getCommandLineValues_singleRegion() {
        String[] args = "-r us-east-1".split(" ");
        CommandLineData data = CSCommandLineParser.getCommandLineValues(args);
        assertEquals(1, data.getRegions().size());
        assertEquals(Region.US_EAST_1, data.getRegions().get(0));
    }

    @Test
    public void test_getCommandLineValues_multipleRegion() {
        String[] args = "-r us-east-1,us-east-2".split(" ");
        CommandLineData data = CSCommandLineParser.getCommandLineValues(args);
        assertEquals(2, data.getRegions().size());
        assertEquals(Region.US_EAST_1, data.getRegions().get(0));
        assertEquals(Region.US_EAST_2, data.getRegions().get(1));
    }

    @Test
    public void test_getCommandLineValues_empty() {
        String[] args = "".split(" ");
        CommandLineData data = CSCommandLineParser.getCommandLineValues(args);
        assertNull(data.getRegions());
    }
}
