package com.cloudSavvy.aws.appstream;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.appstream.AppStreamClient;
import software.amazon.awssdk.services.appstream.model.DescribeFleetsRequest;
import software.amazon.awssdk.services.appstream.model.DescribeFleetsResponse;
import software.amazon.awssdk.services.appstream.model.Fleet;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class AppStreamAccessor {
    private AppStreamClient appStreamClient;

    public List<Fleet> listFleets() {
        String token = null;
        List<Fleet> fleets = new ArrayList<>();
        do {
            DescribeFleetsRequest request = DescribeFleetsRequest.builder().nextToken(token).build();
            DescribeFleetsResponse fleetsResponse = appStreamClient.describeFleets(request);
            fleets.addAll(fleetsResponse.fleets());
            token = fleetsResponse.nextToken();
        } while (token != null && fleets.size() < 1000);
        return fleets;
    }
}
