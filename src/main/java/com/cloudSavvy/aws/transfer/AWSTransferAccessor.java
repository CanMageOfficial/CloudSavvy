package com.cloudSavvy.aws.transfer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.ListedServer;
import software.amazon.awssdk.services.transfer.paginators.ListServersIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class AWSTransferAccessor {
    private TransferClient transferClient;

    public List<ListedServer> listServers() {
        List<ListedServer> servers = new ArrayList<>();

        ListServersIterable serversIterable = transferClient.listServersPaginator();
        for (ListedServer server : serversIterable.servers()) {
            servers.add(server);
            if (servers.size() > 1000) {
                break;
            }
        }

        return servers;
    }
}
