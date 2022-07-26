package com.cloudSavvy.execution;

import com.cloudSavvy.cache.GlobalCache;
import com.cloudSavvy.common.RegionAnalyzeResult;
import com.cloudSavvy.common.run.RunMetadata;
import com.cloudSavvy.common.run.RunStatistics;
import com.cloudSavvy.dagger.AWSConnectorComponent;
import com.cloudSavvy.dagger.DaggerAWSConnectorComponent;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class RegionalExecutor {

    private RunMetadata runMetadata;
    private RunStatistics runStatistics;

    private GlobalCache globalCache;

    public RegionalExecutor(RunStatistics runStatistics, GlobalCache globalCache, RunMetadata runMetadata) {
        this.runStatistics = runStatistics;
        this.globalCache = globalCache;
        this.runMetadata = runMetadata;
    }

    public RegionAnalyzeResult call() {
        AWSConnectorComponent awsConnectorComponent = DaggerAWSConnectorComponent.factory()
                .create(runMetadata, runStatistics, globalCache);
        RegionAnalyzeResult regionAnalyzeResult = new RegionAnalyzeResult(runMetadata.getRegion());

        List<AWSAnalyzer> analyzers = new ArrayList<>(awsConnectorComponent.getAnalyzers());

        analyzers.parallelStream().forEach(analyzer -> regionAnalyzeResult.merge(analyzer.call(runMetadata)));

        return regionAnalyzeResult;
    }
}
