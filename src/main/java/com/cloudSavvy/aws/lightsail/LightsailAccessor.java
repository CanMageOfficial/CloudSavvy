package com.cloudSavvy.aws.lightsail;

import com.cloudSavvy.aws.metric.MetricConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.lightsail.LightsailClient;
import software.amazon.awssdk.services.lightsail.model.ContainerService;
import software.amazon.awssdk.services.lightsail.model.ContainerServiceMetricName;
import software.amazon.awssdk.services.lightsail.model.Disk;
import software.amazon.awssdk.services.lightsail.model.GetContainerServiceMetricDataRequest;
import software.amazon.awssdk.services.lightsail.model.GetContainerServiceMetricDataResponse;
import software.amazon.awssdk.services.lightsail.model.GetContainerServicesRequest;
import software.amazon.awssdk.services.lightsail.model.GetContainerServicesResponse;
import software.amazon.awssdk.services.lightsail.model.GetDisksRequest;
import software.amazon.awssdk.services.lightsail.model.GetDisksResponse;
import software.amazon.awssdk.services.lightsail.model.GetInstanceMetricDataRequest;
import software.amazon.awssdk.services.lightsail.model.GetInstanceMetricDataResponse;
import software.amazon.awssdk.services.lightsail.model.GetInstancesRequest;
import software.amazon.awssdk.services.lightsail.model.GetInstancesResponse;
import software.amazon.awssdk.services.lightsail.model.GetLoadBalancerMetricDataRequest;
import software.amazon.awssdk.services.lightsail.model.GetLoadBalancerMetricDataResponse;
import software.amazon.awssdk.services.lightsail.model.GetLoadBalancersRequest;
import software.amazon.awssdk.services.lightsail.model.GetLoadBalancersResponse;
import software.amazon.awssdk.services.lightsail.model.GetRelationalDatabaseMetricDataRequest;
import software.amazon.awssdk.services.lightsail.model.GetRelationalDatabaseMetricDataResponse;
import software.amazon.awssdk.services.lightsail.model.GetRelationalDatabasesRequest;
import software.amazon.awssdk.services.lightsail.model.GetRelationalDatabasesResponse;
import software.amazon.awssdk.services.lightsail.model.GetStaticIpsRequest;
import software.amazon.awssdk.services.lightsail.model.GetStaticIpsResponse;
import software.amazon.awssdk.services.lightsail.model.Instance;
import software.amazon.awssdk.services.lightsail.model.InstanceMetricName;
import software.amazon.awssdk.services.lightsail.model.LoadBalancer;
import software.amazon.awssdk.services.lightsail.model.LoadBalancerMetricName;
import software.amazon.awssdk.services.lightsail.model.LoadBalancerState;
import software.amazon.awssdk.services.lightsail.model.MetricDatapoint;
import software.amazon.awssdk.services.lightsail.model.MetricStatistic;
import software.amazon.awssdk.services.lightsail.model.MetricUnit;
import software.amazon.awssdk.services.lightsail.model.RelationalDatabase;
import software.amazon.awssdk.services.lightsail.model.RelationalDatabaseMetricName;
import software.amazon.awssdk.services.lightsail.model.StaticIp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class LightsailAccessor {

    private static final String CREATING = "creating";

    private LightsailClient lightsailClient;

    public List<Instance> listInstances() {
        String token = null;
        List<Instance> instances = new ArrayList<>();
        do {
            GetInstancesRequest request = GetInstancesRequest.builder().pageToken(token).build();
            GetInstancesResponse instancesResponse = lightsailClient.getInstances(request);
            instances.addAll(instancesResponse.instances());
            token = instancesResponse.nextPageToken();
        } while (token != null && instances.size() < 1000);
        return instances;
    }

    public List<ContainerService> listContainerServices() {

        GetContainerServicesRequest request = GetContainerServicesRequest.builder().build();
        GetContainerServicesResponse containerServicesResponse = lightsailClient.getContainerServices(request);
        return containerServicesResponse.containerServices();
    }

    public List<RelationalDatabase> listRelationalDatabases() {
        String token = null;
        List<RelationalDatabase> databases = new ArrayList<>();
        do {
            GetRelationalDatabasesRequest request = GetRelationalDatabasesRequest.builder().pageToken(token).build();
            GetRelationalDatabasesResponse databasesResponse = lightsailClient.getRelationalDatabases(request);
            for (RelationalDatabase database : databasesResponse.relationalDatabases()) {
                if (!CREATING.equals(database.state())) {
                    databases.add(database);
                }
            }
            token = databasesResponse.nextPageToken();
        } while (token != null && databases.size() < 1000);
        return databases;
    }

    public List<StaticIp> listUnAttachedStaticIps() {
        String token = null;
        List<StaticIp> staticIps = new ArrayList<>();
        do {
            GetStaticIpsRequest request = GetStaticIpsRequest.builder().pageToken(token).build();
            GetStaticIpsResponse staticIpsResponse = lightsailClient.getStaticIps(request);
            for (StaticIp ip : staticIpsResponse.staticIps()) {
                if (!ip.isAttached()) {
                    staticIps.add(ip);
                }
            }
            token = staticIpsResponse.nextPageToken();
        } while (token != null && staticIps.size() < 1000);
        return staticIps;
    }

    public List<LoadBalancer> listLoadBalancers() {
        String token = null;
        List<LoadBalancer> loadBalancers = new ArrayList<>();
        do {
            GetLoadBalancersRequest request = GetLoadBalancersRequest.builder().pageToken(token).build();
            GetLoadBalancersResponse loadBalancersResponse = lightsailClient.getLoadBalancers(request);
            for (LoadBalancer lb : loadBalancersResponse.loadBalancers()) {
                if (lb.state() != LoadBalancerState.PROVISIONING) {
                    loadBalancers.add(lb);
                }
            }
            token = loadBalancersResponse.nextPageToken();
        } while (token != null && loadBalancers.size() < 1000);
        return loadBalancers;
    }

    public List<Disk> listDisks() {
        String token = null;
        List<Disk> disks = new ArrayList<>();
        do {
            GetDisksRequest request = GetDisksRequest.builder().pageToken(token).build();
            GetDisksResponse disksResponse = lightsailClient.getDisks(request);
            for (Disk disk : disksResponse.disks()) {
                if (!disk.isSystemDisk()) {
                    disks.add(disk);
                }
            }
            token = disksResponse.nextPageToken();
        } while (token != null && disks.size() < 1000);
        return disks;
    }

    public List<MetricDatapoint> getInstanceCPUUtilMetrics(String instanceName) {
        GetInstanceMetricDataRequest request = GetInstanceMetricDataRequest.builder()
                .instanceName(instanceName)
                .metricName(InstanceMetricName.CPU_UTILIZATION)
                .unit(MetricUnit.PERCENT)
                .endTime(Instant.now())
                .startTime(Instant.now().minusSeconds(MetricConstants.SECONDS_IN_TWO_WEEKS))
                .period(MetricConstants.SECONDS_IN_DAY)
                .statistics(MetricStatistic.MAXIMUM).build();
        GetInstanceMetricDataResponse instancesResponse = lightsailClient.getInstanceMetricData(request);
        return instancesResponse.metricData();
    }

    public List<MetricDatapoint> getContainerCPUUtilMetrics(String serviceName) {
        GetContainerServiceMetricDataRequest request = GetContainerServiceMetricDataRequest.builder()
                .serviceName(serviceName)
                .metricName(ContainerServiceMetricName.CPU_UTILIZATION)
                .endTime(Instant.now())
                .startTime(Instant.now().minusSeconds(MetricConstants.SECONDS_IN_TWO_WEEKS))
                .period(MetricConstants.SECONDS_IN_DAY)
                .statistics(MetricStatistic.MAXIMUM).build();
        GetContainerServiceMetricDataResponse instancesResponse = lightsailClient.getContainerServiceMetricData(request);
        return instancesResponse.metricData();
    }

    public List<MetricDatapoint> getRelationalDBConnectionsMetrics(String databaseName) {
        GetRelationalDatabaseMetricDataRequest request = GetRelationalDatabaseMetricDataRequest.builder()
                .relationalDatabaseName(databaseName)
                .metricName(RelationalDatabaseMetricName.DATABASE_CONNECTIONS)
                .unit(MetricUnit.COUNT)
                .endTime(Instant.now())
                .startTime(Instant.now().minusSeconds(MetricConstants.SECONDS_IN_TWO_WEEKS))
                .period(MetricConstants.SECONDS_IN_DAY)
                .statistics(MetricStatistic.SUM).build();
        GetRelationalDatabaseMetricDataResponse instancesResponse = lightsailClient.getRelationalDatabaseMetricData(request);
        return instancesResponse.metricData();
    }

    public List<MetricDatapoint> getLoadBalancerRequestCountMetrics(String loadBalancerName) {
        GetLoadBalancerMetricDataRequest request = GetLoadBalancerMetricDataRequest.builder()
                .loadBalancerName(loadBalancerName)
                .metricName(LoadBalancerMetricName.REQUEST_COUNT)
                .unit(MetricUnit.COUNT)
                .endTime(Instant.now())
                .startTime(Instant.now().minusSeconds(MetricConstants.SECONDS_IN_TWO_WEEKS))
                .period(MetricConstants.SECONDS_IN_DAY)
                .statistics(MetricStatistic.SUM).build();
        GetLoadBalancerMetricDataResponse instancesResponse = lightsailClient.getLoadBalancerMetricData(request);
        return instancesResponse.metricData();
    }
}
