package com.cloudSavvy.commandline;

import com.cloudSavvy.utils.RegionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class CSCommandLineParser {
    private static final String REGION_OPTION = "r";

    public static CommandLineData getCommandLineValues(String[] args) {
        CommandLineData.CommandLineDataBuilder builder = CommandLineData.builder();
        Options options = new Options();

        String desc = "Regions to analyze. "
                + "If not provided, all user regions will be analyzed. "
                + "Sample: \"-r us-east-1,eu-central-1\"  or  \"-r us-east-1\"";
        Option regionOption = Option.builder(REGION_OPTION).longOpt("regions")
                .argName("regions")
                .hasArg()
                .required(false)
                .desc(desc)
                .get();
        options.addOption(regionOption);

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption(REGION_OPTION)) {
                String regionNames = cmd.getOptionValue(REGION_OPTION);
                builder.regions(RegionUtils.parseInputRegions(regionNames));
            }
        } catch (ParseException e) {
            log.debug("Parsing command line failed.", e);
            try {
                HelpFormatter.builder().get().printHelp("Usage:", "", options, "", false);
            } catch (java.io.IOException ignored) {
            }
            System.exit(0);
        }
        return builder.build();
    }
}