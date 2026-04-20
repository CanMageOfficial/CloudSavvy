package com.cloudSavvy.aws.ecr;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.ImageDetail;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.paginators.DescribeRepositoriesIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class EcrAccessor {

    private EcrClient ecrClient;

    public List<Repository> listRepositories() {
        DescribeRepositoriesIterable iterable = ecrClient.describeRepositoriesPaginator();
        List<Repository> repositories = new ArrayList<>();
        for (DescribeRepositoriesResponse response : iterable) {
            repositories.addAll(response.repositories());
            if (repositories.size() > 1000) {
                break;
            }
        }
        return repositories;
    }

    public List<ImageDetail> listImages(String repositoryName) {
        return ecrClient.describeImages(DescribeImagesRequest.builder()
                .repositoryName(repositoryName)
                .maxResults(1)
                .build()).imageDetails();
    }
}
