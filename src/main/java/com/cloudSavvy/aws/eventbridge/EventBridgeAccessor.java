package com.cloudSavvy.aws.eventbridge;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.Archive;
import software.amazon.awssdk.services.eventbridge.model.ListArchivesRequest;
import software.amazon.awssdk.services.eventbridge.model.ListArchivesResponse;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class EventBridgeAccessor {

    private EventBridgeClient eventBridgeClient;

    public List<Archive> listArchives() {
        ListArchivesRequest request = ListArchivesRequest.builder().build();
        ListArchivesResponse archivesResponse = eventBridgeClient.listArchives(request);
        return archivesResponse.archives();
    }
}
