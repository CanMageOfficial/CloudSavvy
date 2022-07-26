package com.cloudSavvy.aws.ec2;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesResponse;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.NatGatewayState;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeStatusItem;
import software.amazon.awssdk.services.ec2.paginators.DescribeInstancesIterable;
import software.amazon.awssdk.services.ec2.paginators.DescribeNatGatewaysIterable;
import software.amazon.awssdk.services.ec2.paginators.DescribeVolumeStatusIterable;
import software.amazon.awssdk.services.ec2.paginators.DescribeVolumesIterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class EC2Accessor {

    private Ec2Client ec2Client;

    public List<Address> listAddresses() {
        DescribeAddressesResponse addressesResponse = ec2Client.describeAddresses();
        List<Address> addresses = addressesResponse.addresses();
        log.debug("Addresses count: {}", addresses.size());
        return addresses;
    }

    public List<NatGateway> listNATGateways() {
        List<NatGateway> gateways = new ArrayList<>();
        DescribeNatGatewaysIterable gatewaysIterable = ec2Client.describeNatGatewaysPaginator();

        for (NatGateway natGateway : gatewaysIterable.natGateways()) {
            if (natGateway.state() != NatGatewayState.DELETED
                    && natGateway.state() != NatGatewayState.DELETING) {
                gateways.add(natGateway);
            }
            if (gateways.size() > 1000) {
                break;
            }
        }
        log.debug("NatGateways count: {}", gateways.size());
        return gateways;
    }

    public List<Reservation> listInstances() {
        List<Reservation> reservations = new ArrayList<>();
        DescribeInstancesIterable instancesIterable = ec2Client.describeInstancesPaginator();

        for (Reservation reservation : instancesIterable.reservations()) {
            reservations.add(reservation);

            if (reservations.size() > 1000) {
                break;
            }
        }
        log.debug("Instances count: {}", reservations.size());
        return reservations;
    }

    public List<Volume> listVolumes() {
        DescribeVolumesIterable volumesIterable = ec2Client.describeVolumesPaginator();
        List<Volume> volumes = new ArrayList<>();

        for (Volume volume : volumesIterable.volumes()) {
            volumes.add(volume);

            if (volumes.size() > 1000) {
                break;
            }
        }
        log.debug("Volumes count: {}", volumes.size());
        return volumes;
    }

    public Map<String, VolumeStatusItem> listVolumeStatuses() {
        DescribeVolumeStatusIterable volumeStatusIterable = ec2Client.describeVolumeStatusPaginator();

        List<VolumeStatusItem> volumeStatuses = new ArrayList<>();
        for (VolumeStatusItem volumeStatusItem : volumeStatusIterable.volumeStatuses()) {
            volumeStatuses.add(volumeStatusItem);
        }

        log.debug("Volume statuses count: {}", volumeStatuses.size());

        Map<String, VolumeStatusItem> idToStatusMap = new HashMap<>(volumeStatuses.size());
        for (VolumeStatusItem item : volumeStatuses) {
            idToStatusMap.put(item.volumeId(), item);
        }
        return idToStatusMap;
    }
}
