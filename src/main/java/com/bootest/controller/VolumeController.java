package com.bootest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.volume.AttachmentDataDto;
import com.bootest.dto.volume.AttachmentDto;
import com.bootest.model.*;
import com.bootest.repository.*;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public List<RecoVolume> findAll(
            @RequestParam(name = "volumeId", required = false) String volumeId) {
        SearchBuilder<RecoVolume> searchBuilder = SearchBuilder.builder();

        if (volumeId != null) {
            searchBuilder.with("volumeId", SearchOperationType.EQUAL, volumeId);
        }

        return volumeRepo.findAll(searchBuilder.build());

    }

    @GetMapping("/get")
    public List<RecoVolume> getVolumeData() throws JsonProcessingException {

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

                        RecoVolume recoVolume = volumeRepo.findByVolumeId(v.volumeId())
                                .orElseGet(() -> {
                                    RecoVolume rv = new RecoVolume();
                                    rv.setId(UUID.randomUUID().toString());
                                    rv.setVolumeId(v.volumeId());
                                    return rv;
                                });

                        if (!v.attachments().isEmpty()) {
                            for (VolumeAttachment va : v.attachments()) {
                                recoVolume.setInstanceId(va.instanceId());
                            }
                        } else {
                            recoVolume.setInstanceId(null);
                        }

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
