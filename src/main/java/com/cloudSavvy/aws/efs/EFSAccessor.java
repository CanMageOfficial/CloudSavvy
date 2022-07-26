package com.cloudSavvy.aws.efs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.DescribeFileSystemsResponse;
import software.amazon.awssdk.services.efs.model.DescribeLifecycleConfigurationRequest;
import software.amazon.awssdk.services.efs.model.DescribeLifecycleConfigurationResponse;
import software.amazon.awssdk.services.efs.model.DescribeMountTargetsRequest;
import software.amazon.awssdk.services.efs.model.DescribeMountTargetsResponse;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.LifeCycleState;
import software.amazon.awssdk.services.efs.model.LifecyclePolicy;
import software.amazon.awssdk.services.efs.model.MountTargetDescription;
import software.amazon.awssdk.services.efs.paginators.DescribeFileSystemsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class EFSAccessor {

    private EfsClient efsClient;

    public List<FileSystemDescription> listFileSystems() {
        DescribeFileSystemsIterable fileSystemsIterable = efsClient.describeFileSystemsPaginator();
        List<FileSystemDescription> fileSystems = new ArrayList<>();
        for (DescribeFileSystemsResponse fileSystemsResponse : fileSystemsIterable) {
            for (FileSystemDescription fileSystem : fileSystemsResponse.fileSystems()) {
                if (!(fileSystem.lifeCycleState() == LifeCycleState.CREATING)
                        && !(fileSystem.lifeCycleState() == LifeCycleState.DELETED)
                        && !(fileSystem.lifeCycleState() == LifeCycleState.DELETING)) {
                    fileSystems.add(fileSystem);
                }
            }

            if (fileSystems.size() > 1000) {
                break;
            }
        }
        return fileSystems;
    }

    public List<MountTargetDescription> listMountTargets(String fileSystemId) {
        DescribeMountTargetsRequest request = DescribeMountTargetsRequest.builder()
                .fileSystemId(fileSystemId).build();
        DescribeMountTargetsResponse mountTargetsResponse = efsClient.describeMountTargets(request);
        return mountTargetsResponse.mountTargets();
    }

    public List<LifecyclePolicy> listLifecyclePolicies(String fileSystemId) {
        DescribeLifecycleConfigurationRequest request = DescribeLifecycleConfigurationRequest.builder()
                .fileSystemId(fileSystemId).build();
        DescribeLifecycleConfigurationResponse mountTargetsResponse = efsClient.describeLifecycleConfiguration(request);
        return mountTargetsResponse.lifecyclePolicies();
    }
}
