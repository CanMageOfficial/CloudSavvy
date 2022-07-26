package com.cloudSavvy.aws.secretsmanager;

import com.cloudSavvy.aws.common.ResourceAge;
import com.cloudSavvy.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;
import software.amazon.awssdk.services.secretsmanager.paginators.ListSecretsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class SecretsManagerAccessor {

    private SecretsManagerClient secretsManagerClient;

    public List<SecretListEntry> listUnusedSecrets() {
        ListSecretsIterable clustersIterable = secretsManagerClient.listSecretsPaginator();
        List<SecretListEntry> secretListEntries = new ArrayList<>();
        for (ListSecretsResponse listClustersResponse : clustersIterable) {
            for (SecretListEntry entry : listClustersResponse.secretList()) {
                if (TimeUtils.getElapsedTimeInDays(entry.lastAccessedDate()) > ResourceAge.SIX_MONTHS) {
                    secretListEntries.add(entry);
                }
            }

            if (secretListEntries.size() > 10) {
                break;
            }
        }
        return secretListEntries;
    }
}
