package com.cloudSavvy.aws.stepfunctions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.ListStateMachinesResponse;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;
import software.amazon.awssdk.services.sfn.paginators.ListStateMachinesIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class StepFunctionsAccessor {

    private SfnClient sfnClient;

    public List<StateMachineListItem> listStateMachines() {
        ListStateMachinesIterable iterable = sfnClient.listStateMachinesPaginator();
        List<StateMachineListItem> stateMachines = new ArrayList<>();
        for (ListStateMachinesResponse response : iterable) {
            stateMachines.addAll(response.stateMachines());
            if (stateMachines.size() > 1000) {
                break;
            }
        }
        return stateMachines;
    }
}
