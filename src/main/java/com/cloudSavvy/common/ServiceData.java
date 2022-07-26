package com.cloudSavvy.common;

import com.cloudSavvy.aws.common.EntityType;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class ServiceData {

    public ServiceData(EntityType entityType, List<ResourceMetadata> resources) {
        this.entityType = entityType;
        this.resources = resources;
    }

    private EntityType entityType;
    private List<ResourceMetadata> resources;
}
