package com.cloudSavvy.aws.cloudformation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;
import software.amazon.awssdk.services.cloudformation.paginators.DescribeStacksIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class CloudFormationAccessor {

    private CloudFormationClient cloudFormationClient;

    public List<Stack> listFailedStacks() {
        DescribeStacksIterable iterable = cloudFormationClient.describeStacksPaginator();
        List<Stack> failedStacks = new ArrayList<>();
        for (DescribeStacksResponse response : iterable) {
            for (Stack stack : response.stacks()) {
                StackStatus status = stack.stackStatus();
                if (status == StackStatus.ROLLBACK_COMPLETE
                        || status == StackStatus.DELETE_FAILED
                        || status == StackStatus.CREATE_FAILED
                        || status == StackStatus.UPDATE_ROLLBACK_FAILED
                        || status == StackStatus.IMPORT_ROLLBACK_FAILED) {
                    failedStacks.add(stack);
                }
            }
            if (failedStacks.size() > 1000) {
                break;
            }
        }
        return failedStacks;
    }
}
