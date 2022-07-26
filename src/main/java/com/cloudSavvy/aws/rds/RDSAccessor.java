package com.cloudSavvy.aws.rds;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBCluster;
import software.amazon.awssdk.services.rds.model.DBClusterSnapshot;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DBProxy;
import software.amazon.awssdk.services.rds.model.DBProxyStatus;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterSnapshotsRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbProxiesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbProxyTargetsRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbProxyTargetsResponse;
import software.amazon.awssdk.services.rds.paginators.DescribeDBClusterSnapshotsIterable;
import software.amazon.awssdk.services.rds.paginators.DescribeDBClustersIterable;
import software.amazon.awssdk.services.rds.paginators.DescribeDBInstancesIterable;
import software.amazon.awssdk.services.rds.paginators.DescribeDBProxiesIterable;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RDSAccessor {

    private RdsClient rdsClient;

    private static final String DELETING = "deleting";
    private static final String CREATING = "creating";
    private static final String MANUAL = "manual";

    public List<DBInstance> listNonClusterDBInstances() {
        DescribeDBInstancesIterable dbInstancesIterable = rdsClient.describeDBInstancesPaginator();
        List<DBInstance> dbInstances = new ArrayList<>();
        for (DBInstance instance : dbInstancesIterable.dbInstances()) {
            if (StringUtils.isEmpty(instance.dbClusterIdentifier())
                    && !DELETING.equalsIgnoreCase(instance.dbInstanceStatus())
                    && !CREATING.equalsIgnoreCase(instance.dbInstanceStatus())) {
                dbInstances.add(instance);
            }

            if (dbInstances.size() > 1000) {
                break;
            }
        }
        log.debug("RDS DB Instances count: {}", dbInstances.size());
        return dbInstances;
    }

    // https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/accessing-monitoring.html
    public List<DBCluster> listDBClusters() {
        DescribeDBClustersIterable dbClustersIterable = rdsClient.describeDBClustersPaginator();
        List<DBCluster> dbClusters = new ArrayList<>();
        for (DBCluster cluster : dbClustersIterable.dbClusters()) {
            if (!DELETING.equalsIgnoreCase(cluster.status()) && !CREATING.equalsIgnoreCase(cluster.status())) {
                dbClusters.add(cluster);
            }

            if (dbClusters.size() > 1000) {
                break;
            }
        }
        log.debug("DB Clusters count: {}", dbClusters.size());
        return dbClusters;
    }

    public List<DBProxy> listDBProxies() {
        DescribeDbProxiesRequest request = DescribeDbProxiesRequest.builder().build();
        DescribeDBProxiesIterable dbProxiesIterable = rdsClient.describeDBProxiesPaginator(request);
        List<DBProxy> dbProxies = new ArrayList<>();
        for (DBProxy proxy : dbProxiesIterable.dbProxies()) {
            if (proxy.status() != DBProxyStatus.DELETING && proxy.status() != DBProxyStatus.CREATING) {
                dbProxies.add(proxy);
            }

            if (dbProxies.size() > 1000) {
                break;
            }
        }
        log.debug("DB proxies count: {}", dbProxies.size());
        return dbProxies;
    }

    public int getDbProxyTargetCount(String proxyName) {
        DescribeDbProxyTargetsRequest request =
                DescribeDbProxyTargetsRequest.builder().dbProxyName(proxyName).build();
        DescribeDbProxyTargetsResponse response = rdsClient.describeDBProxyTargets(request);
        return response.targets().size();
    }

    public List<DBClusterSnapshot> listDBClusterSnapshots() {
        DescribeDbClusterSnapshotsRequest request =
                DescribeDbClusterSnapshotsRequest.builder().snapshotType(MANUAL).build();
        DescribeDBClusterSnapshotsIterable response = rdsClient.describeDBClusterSnapshotsPaginator(request);
        List<DBClusterSnapshot> snapshots = new ArrayList<>();
        for (DBClusterSnapshot snapshot : response.dbClusterSnapshots()) {
            snapshots.add(snapshot);

            if (snapshots.size() > 1000) {
                break;
            }
        }
        return snapshots;
    }
}
