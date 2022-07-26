package com.cloudSavvy.aws.fsx;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.fsx.FSxClient;
import software.amazon.awssdk.services.fsx.model.DescribeFileSystemsResponse;
import software.amazon.awssdk.services.fsx.model.FileSystem;
import software.amazon.awssdk.services.fsx.paginators.DescribeFileSystemsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class FSxAccessor {
    private FSxClient fsxClient;

    public List<FileSystem> listFileSystem() {
        List<FileSystem> fileSystems = new ArrayList<>();
        DescribeFileSystemsIterable fileSystemsIterable = fsxClient.describeFileSystemsPaginator();
        for (DescribeFileSystemsResponse fileSystem : fileSystemsIterable) {
            fileSystems.addAll(fileSystem.fileSystems());
        }
        return fileSystems;
    }
}
