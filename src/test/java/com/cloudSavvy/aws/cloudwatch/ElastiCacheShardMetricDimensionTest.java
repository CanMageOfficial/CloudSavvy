package com.cloudSavvy.aws.cloudwatch;

import com.cloudSavvy.aws.metric.ElastiCacheShardMetricDimension;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElastiCacheShardMetricDimensionTest {
    @Test
    public void test_buildDimensions() {
        String repGroupId = "testShardCluster";
        String nodeGroupId = "0002";
        String replicationGroupId = "ReplicationGroupId";
        String dimGroupId =
                ElastiCacheShardMetricDimension.buildDimensionGroup(repGroupId, nodeGroupId);
        ElastiCacheShardMetricDimension dimensionBuilder = new ElastiCacheShardMetricDimension();
        Collection<Dimension> dimensions = dimensionBuilder.buildDimensions(replicationGroupId, dimGroupId, null);
        assertEquals(2, dimensions.size());
        List<Dimension> repGroupIdDim = dimensions.stream().filter(dim -> dim.name().equals(replicationGroupId) &&
                dim.value().equals(repGroupId)).collect(Collectors.toList());
        assertEquals(1, repGroupIdDim.size());
        List<Dimension> nodeGroupIdDim = dimensions.stream().filter(dim -> dim.name().equals("NodeGroupId") &&
                dim.value().equals(nodeGroupId)).collect(Collectors.toList());
        assertEquals(1, nodeGroupIdDim.size());
    }
}
