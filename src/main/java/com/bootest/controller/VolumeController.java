package com.bootest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.volume.AttachmentDataDto;
import com.bootest.dto.volume.AttachmentDto;
import com.bootest.model.*;
import com.bootest.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttachment;

@RestController
@RequiredArgsConstructor
@RequestMapping("/volume")
public class VolumeController {

    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;
    private final RecoVolumeRepo volumeRepo;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<RecoVolume> findAll() throws JsonProcessingException {

        List<RecoVolume> results = new ArrayList<>();

        String nextToken = null;

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {

            String[] regionArr = account.getRegions().split(", ");

            for (String s : regionArr) {
                Region region = Region.of(s);

                Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);

                DescribeVolumesRequest request = DescribeVolumesRequest.builder()
                        .maxResults(100)
                        .nextToken(nextToken)
                        .build();

                do {

                    DescribeVolumesResponse response = ec2.describeVolumes(request);

                    for (Volume v : response.volumes()) {
                        List<AttachmentDataDto> attachedIds = new ArrayList<>();

                        for (VolumeAttachment va : v.attachments()) {
                            AttachmentDataDto attachData = new AttachmentDataDto();
                            attachData.setInstanceId(va.instanceId());
                            attachData.setDeviceName(va.device());
                            attachedIds.add(attachData);
                        }

                        AttachmentDto attachments = new AttachmentDto();
                        attachments.setData(attachedIds);

                        RecoVolume recoVolume = new RecoVolume();
                        recoVolume.setId(UUID.randomUUID().toString());
                        recoVolume.setVolumeId(v.volumeId());
                        recoVolume.setInstanceId(v.attachments().get(0).instanceId());
                        recoVolume.setVolumeType(v.volumeType());
                        recoVolume.setAvailabilityZone(v.availabilityZone());
                        recoVolume.setCreateTime(v.createTime().toString());
                        recoVolume.setEncrypted(v.encrypted());
                        recoVolume.setSize(v.size().shortValue());
                        recoVolume.setSnapshotId(v.snapshotId());
                        recoVolume.setAttachments(objectMapper.writeValueAsString(attachments));
                        recoVolume.setState(v.stateAsString());

                        results.add(recoVolume);
                    }
                } while (nextToken != null);
            }
        }
        return volumeRepo.saveAll(results);
    }

}
