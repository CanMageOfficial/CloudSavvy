package com.cloudSavvy.frontend;

import com.cloudSavvy.common.run.RunStatistics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RunScreen implements Runnable {

    RunStatistics runStatistics;

    @Override
    public void run() {
        RunStatistics.RunData runData = runStatistics.getRunMetric();
        if (!runData.getData().isEmpty()) {
            log.info("{}{}", System.lineSeparator(), runData);
        }
    }
}
