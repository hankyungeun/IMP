package com.bootest.controller;

import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeStatusResponse;
import software.amazon.awssdk.services.ec2.model.VolumeStatusItem;

public class DescribeVolumeStatus {
    public static void main(String[] args) {
        List<AwsBasicCredentials> credentialList = List.of(
            AwsBasicCredentials.create("", ""));

        List<Region> regionList = List.of(
            Region.AP_NORTHEAST_2
        );

        for (AwsBasicCredentials abc : credentialList) {
            for (Region region : regionList)
            {
                Ec2Client ec2 = Ec2Client.builder().credentialsProvider(StaticCredentialsProvider.create(abc)).region(region).build();
                describeVolumeHealth(ec2);
                ec2.close();
            }
        }
    }

    public static void describeVolumeHealth(Ec2Client ec2) {
        DescribeVolumeStatusRequest req = DescribeVolumeStatusRequest.builder()
        .volumeIds("vol-0fa07eb97676d09f1")
        .build();

        DescribeVolumeStatusResponse res = ec2.describeVolumeStatus(req);

        for (VolumeStatusItem vsi : res.volumeStatuses()) {
            System.out.println(vsi);
        }
    }
}
