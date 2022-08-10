package com.bootest.controller;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Volume;

public class DescribeVolume {
    public static void main(String[] args) {

        AwsBasicCredentials abc = AwsBasicCredentials.create("", "");

        String values = "i-0de4b250800d4cac8"; //InstanceId

        Region region = Region.AP_NORTHEAST_2;
        Ec2Client ec2 = Ec2Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(abc))
            .region(region)
            .build();

        describeVolume(ec2, values);
        ec2.close();
    }

    public static void describeVolume(Ec2Client ec2, String values){

        Filter f1 = Filter.builder().name("attachment.instance-id").values(values).build();
        //Filter f2 = Filter.builder().name("attachment.delete-on-termination").values("false").build();

        try {
            DescribeVolumesRequest dvr = DescribeVolumesRequest.builder().filters(f1).build();
            DescribeVolumesResponse response = ec2.describeVolumes(dvr);


            for(Volume v : response.volumes())
            {


                
                System.out.println(v);
            }
    
            //List<Volume> volumes = response.volumes();
            
        } catch (Exception e) {
            System.out.println("volume Id is not there");
            throw e;
        }
    }
}
