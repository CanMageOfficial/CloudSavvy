package com.cloudSavvy.aws.ecs;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.Service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ECSUtilsTest {
    private final String DEFAULT_CLUSTER_ARN = "arn:aws:ecs:us-east-2:771793231253:cluster/default";
    private final String INVALID = "invalid";
    final String DEFAULT = "default";

    @Test
    public void test_getClusterName_null() {
        String clusterName = ECSUtils.getClusterName(null);
        assertNull(clusterName);
    }

    @Test
    public void test_getClusterName_invalid() {
        String clusterName = ECSUtils.getClusterName(INVALID);
        assertNull(clusterName);
    }

    @Test
    public void test_getClusterName_success() {
        String clusterName = ECSUtils.getClusterName(DEFAULT_CLUSTER_ARN);
        assertEquals(DEFAULT, clusterName);
    }

    @Test
    public void test_getActiveServiceCount() {
        List<Cluster> clusters = List.of(Cluster.builder().activeServicesCount(2).build(),
                Cluster.builder().activeServicesCount(0).build(),
                Cluster.builder().activeServicesCount(1).build(),
                Cluster.builder().build());
        assertEquals(3, ECSUtils.getActiveServiceCount(clusters));
    }

    @Test
    public void test_getActiveServiceCount_Empty() {
        List<Cluster> clusters = List.of(Cluster.builder().build());
        assertEquals(0, ECSUtils.getActiveServiceCount(clusters));
    }

    @Test
    public void test_getClusterNameToServicesMap() {
        String TEST_CLUSTER_ARN = "arn:aws:ecs:us-east-2:771793231253:cluster/test";

        List<Service> services = List.of(buildService(DEFAULT_CLUSTER_ARN), buildService(DEFAULT_CLUSTER_ARN),
                buildService(TEST_CLUSTER_ARN), buildService(INVALID), Service.builder().build());
        Map<String, List<Service>> result = ECSUtils.getClusterNameToServicesMap(services);
        assertEquals(2, result.size());
        assertEquals(1, result.get("test").size());
        assertEquals(2, result.get(DEFAULT).size());
    }

    private Service buildService(String clusterArn) {
        return Service.builder().clusterArn(clusterArn).build();
    }
}
