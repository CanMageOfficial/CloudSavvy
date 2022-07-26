package com.cloudSavvy.common.run;

import com.cloudSavvy.aws.common.AWSService;
import lombok.Data;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunStatistics {
    private final ConcurrentHashMap<Region, ConcurrentHashMap<AWSService, RunStatus>> runStatus = new ConcurrentHashMap<>();

    public void startRun(final Region region, final AWSService awsService) {
        ConcurrentHashMap<AWSService, RunStatus> serviceStatus =
                runStatus.getOrDefault(region, new ConcurrentHashMap<>());
        serviceStatus.put(awsService, RunStatus.RUNNING);
        runStatus.put(region, serviceStatus);
    }

    public void finishRun(final Region region, final AWSService awsService, final boolean succeeded) {
        ConcurrentHashMap<AWSService, RunStatus> serviceStatus =
                runStatus.getOrDefault(region, new ConcurrentHashMap<>());
        serviceStatus.put(awsService, succeeded ? RunStatus.SUCCEEDED : RunStatus.FAILED);
        runStatus.put(region, serviceStatus);
    }

    public RunData getRunMetric() {
        RunData runData = new RunData();

        for (Map.Entry<Region, ConcurrentHashMap<AWSService, RunStatus>> entry : runStatus.entrySet()) {
            Region region = entry.getKey();
            runData.getData().put(region, new HashMap<>());
            ConcurrentHashMap<AWSService, RunStatus> serviceStatus = runStatus.get(region);
            for (Map.Entry<AWSService, RunStatus> serviceStatusEntry : serviceStatus.entrySet()) {
                List<AWSService> services = runData.getData().get(region)
                        .getOrDefault(serviceStatusEntry.getValue(), new ArrayList<>());
                services.add(serviceStatusEntry.getKey());
                runData.getData().get(region).put(serviceStatusEntry.getValue(), services);
            }
        }


        return runData;
    }

    @Data
    public static class RunData {
        private Map<Region, Map<RunStatus, List<AWSService>>> data;

        public RunData() {
            data = new HashMap<>();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Region, Map<RunStatus, List<AWSService>>> regionEntry : data.entrySet()) {
                Map<RunStatus, List<AWSService>> serviceList =  regionEntry.getValue();
                if (serviceList.containsKey(RunStatus.RUNNING)) {
                    sb.append(regionEntry.getKey()).append(": ");
                    sb.append(RunStatus.RUNNING).append(":")
                            .append(serviceList.get(RunStatus.RUNNING));
                } else {
                    continue;
                }
                if (serviceList.containsKey(RunStatus.SUCCEEDED)) {
                    sb.append(", ").append(RunStatus.SUCCEEDED).append(":")
                            .append(serviceList.get(RunStatus.SUCCEEDED).size());
                }
                if (serviceList.containsKey(RunStatus.FAILED)) {
                    sb.append(", ").append(RunStatus.FAILED).append(":")
                            .append(serviceList.get(RunStatus.FAILED).size());
                }
                sb.append(System.lineSeparator());
            }

            return sb.toString();
        }
    }
}
