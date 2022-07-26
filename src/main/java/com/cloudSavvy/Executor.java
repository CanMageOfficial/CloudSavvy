package com.cloudSavvy;

import com.cloudSavvy.commandline.CSCommandLineParser;
import com.cloudSavvy.commandline.CommandLineData;
import com.cloudSavvy.dagger.DaggerExecutorComponent;
import com.cloudSavvy.dagger.ExecutorComponent;
import com.cloudSavvy.execution.ExecutionInput;
import com.cloudSavvy.execution.LambdaExecutor;
import com.cloudSavvy.model.StartRunRequest;
import com.cloudSavvy.utils.EnvironmentUtils;
import com.yworks.util.annotation.Obfuscation;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Obfuscation()
public class Executor {

    public static void main(String[] args) {
        // This code is only to simulate lambda environments in local machine during development
        if (EnvironmentUtils.isRunningInLambda()) {
            LambdaExecutor lambdaExecutor = new LambdaExecutor();
            lambdaExecutor.handleRequest(StartRunRequest.builder().build(), null);
        } else {
            ExecutorComponent executorComponent = DaggerExecutorComponent.create();
            String accountId = executorComponent.getStsClient().getCallerIdentity().account();
            Path outputDirectoryPath = buildOutputPath(accountId);
            CommandLineData commandLineData = CSCommandLineParser.getCommandLineValues(args);
            ExecutionInput input = ExecutionInput.builder().requestedRegions(commandLineData.getRegions())
                    .outputDirectoryPath(outputDirectoryPath).build();
            executorComponent.getGlobalExecutor().execute(input);
        }
    }

    private static Path buildOutputPath(String accountId) {
        Path curDir = Paths.get("").toAbsolutePath();
        Path resultsDir = curDir.resolve("Results").resolve(accountId);
        try {
            if (!Files.exists(resultsDir)) {
                Files.createDirectories(resultsDir);
            }
        } catch (Exception exc) {
            log.error("Cannot create 'Results' dir", exc);
            resultsDir = curDir;
        }

        return resultsDir;
    }
}
