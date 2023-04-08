package com.bootest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.volume.DescribeVolumeDataDto;
import com.bootest.dto.volume.DescribeVolumeDto;
import com.bootest.dto.volume.ModifyVolumeDto;
import com.bootest.dto.volume.VolumeAttachmentSpecification;
import com.bootest.model.Account;
import com.bootest.model.ResultObject;
import com.bootest.repository.AccountRepo;
import com.bootest.util.VrmTags;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteVolumeRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ModifyVolumeRequest;
import software.amazon.awssdk.services.ec2.model.ModifyVolumeResponse;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttachment;

@RequiredArgsConstructor
@Slf4j
@Service
public class VolumeService {

    private final Ec2ResDescribeService descService;
    private final Ec2ClientManager awscm;
    private final AccountRepo accountRepo;

    public List<DescribeVolumeDto> findAll() {
        List<DescribeVolumeDto> results = new ArrayList<>();
        List<Account> accounts = accountRepo.findAll();

        for (Account a : accounts) {
            String[] regionArr = a.getRegions().split(", ");

            for (String regionStr : regionArr) {
                Region region = Region.of(regionStr);

                Ec2Client ec2 = awscm.getEc2WithAccount(region, a);

                List<DescribeVolumeDataDto> data = getVolumeSpecifications(ec2, null);

                results.add(new DescribeVolumeDto(a.getAccountId(), a.getName(), regionStr, data));
            }
        }
        return results;
    }

    public List<DescribeVolumeDto> findByRegion(String regionId) {
        List<DescribeVolumeDto> results = new ArrayList<>();
        List<Account> accounts = accountRepo.findAll();

        for (Account a : accounts) {
            Region region = Region.of(regionId);

            Ec2Client ec2 = awscm.getEc2WithAccount(region, a);

            List<DescribeVolumeDataDto> data = getVolumeSpecifications(ec2, null);

            DescribeVolumeDto volume = new DescribeVolumeDto();
            volume.setAccountId(a.getAccountId());
            volume.setAccountName(a.getName());
            volume.setRegionId(regionId);
            volume.setData(data);

            results.add(volume);
        }
        return results;
    }

    public DescribeVolumeDto findByAccount(String accountId, String regionId) {
        Account account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID: " + accountId));

        Region region = Region.of(regionId);

        Ec2Client ec2 = awscm.getEc2WithAccount(region, account);

        List<DescribeVolumeDataDto> data = getVolumeSpecifications(ec2, null);

        DescribeVolumeDto volume = new DescribeVolumeDto();
        volume.setAccountId(account.getAccountId());
        volume.setAccountName(account.getName());
        volume.setRegionId(regionId);
        volume.setData(data);

        return volume;
    }

    public DescribeVolumeDto findById(String accountId, String regionId, String id) {
        Account account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID: " + accountId));

        Region region = Region.of(regionId);

        Ec2Client ec2 = awscm.getEc2WithAccount(region, account);

        List<DescribeVolumeDataDto> data = getVolumeSpecifications(ec2, id);

        DescribeVolumeDto volume = new DescribeVolumeDto();
        volume.setAccountId(account.getAccountId());
        volume.setAccountName(account.getName());
        volume.setRegionId(regionId);
        volume.setData(data);

        return volume;
    }

    public ResultObject modify(ModifyVolumeDto temp) {
        Account account = accountRepo.findByAccountId(temp.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID: " + temp.getAccountId()));

        Region region = Region.of(temp.getRegionId());

        Ec2Client ec2 = awscm.getEc2WithAccount(region, account);

        return modifyVolume(ec2, temp.getVolumeId(), temp.getVolumeType(), temp.getMultiAttachEnabled(), temp.getIops(),
                temp.getSize(), temp.getThroughput());
    }

    /**
     * Request to delete a specified volume with id.
     * 
     * @author minsoo
     * @param ec2
     * @param volumeId
     * @return
     */
    public ResultObject deleteVolume(
            Ec2Client ec2,
            String volumeId) {
        ResultObject result = new ResultObject();
        log.info("Delete Volume Request Start (volumeId={})", volumeId);

        DeleteVolumeRequest request = DeleteVolumeRequest.builder()
                .volumeId(volumeId)
                .build();

        try {
            ec2.deleteVolume(request);
            log.info("Delete Volume Request Complete (volumeId={})", volumeId);
            result.setResult(true);
            result.setMessage("Delete Volume Request Complete");
            return result;
        } catch (Exception e) {
            log.error("Delete Volume Request Failed (Message: {})", e.getMessage(), e);
            result.setResult(false);
            result.setMessage("Delete Volume Request Failed (Message: " + e.getMessage() + ")");
            return result;
        }
    }

    public ResultObject modifyVolume(
            Ec2Client ec2,
            String volumeId,
            String volumeType,
            Boolean multiAttachEnabled,
            Integer iops,
            Integer size,
            Integer throughput) {
        ResultObject result = new ResultObject();
        log.info("Modify Volume Request Start (volumeId={})", volumeId);

        ModifyVolumeRequest.Builder requestBuilder = ModifyVolumeRequest.builder()
                .volumeId(volumeId);

        if (volumeType != null) {
            requestBuilder.volumeType(volumeType);
        }

        if (multiAttachEnabled != null) {
            if (multiAttachEnabled) {
                requestBuilder.multiAttachEnabled(multiAttachEnabled);
            }
        }

        if (iops != null) {
            requestBuilder.iops(iops);
        }

        if (size != null) {
            requestBuilder.size(size);
        }

        if (throughput != null) {
            requestBuilder.throughput(throughput);
        }

        try {
            ModifyVolumeResponse response = ec2.modifyVolume(requestBuilder.build());
            log.info("Modify Volume Request Complete (volumeId={})", volumeId);
            result.setData(response.volumeModification().volumeId());
            result.setMessage("Modify Volume Request Complete");
            result.setResult(true);
            return result;
        } catch (Ec2Exception e) {
            log.error("Modify Volume Request Failed (Message: {})", e.getMessage(), e);
            result.setMessage("Modify Volume Request Failed (Message: " + e.getMessage() + ")");
            result.setResult(false);
            return result;
        }
    }

    public List<DescribeVolumeDataDto> getVolumeSpecifications(Ec2Client ec2, String volumeId) {

        List<DescribeVolumeDataDto> results = new ArrayList<>();

        List<Volume> volumes = descService.getVolumeDesc(ec2, volumeId, null);

        if (volumes == null) {
            return null;
        }

        for (Volume v : volumes) {

            DescribeVolumeDataDto vs = new DescribeVolumeDataDto();
            vs.setVolumeId(v.volumeId());
            vs.setVolumeType(v.volumeTypeAsString());
            vs.setSize(v.size());
            vs.setVolumeState(v.stateAsString());
            vs.setIops(v.iops());
            vs.setThroughput(v.throughput());
            vs.setEncrypted(v.encrypted());
            vs.setKmsKeyId(v.kmsKeyId());
            vs.setSnapshotId(v.snapshotId());
            vs.setAvailabilityZone(v.availabilityZone());
            vs.setCreated(v.createTime());
            vs.setMultiAttachEnabled(v.multiAttachEnabled());
            vs.setAttachmentSpecifications(getVolumeAttachmentSpecifications(v.attachments()));
            vs.setTags(VrmTags.getEc2Tags(v.tags()));

            results.add(vs);
        }
        return results;
    }

    public List<VolumeAttachmentSpecification> getVolumeAttachmentSpecifications(List<VolumeAttachment> vaList) {

        List<VolumeAttachmentSpecification> results = new ArrayList<>();

        for (VolumeAttachment va : vaList) {

            VolumeAttachmentSpecification vas = new VolumeAttachmentSpecification();
            vas.setInstanceId(va.instanceId());
            vas.setDeviceName(va.device());
            vas.setDeleteOnTermination(va.deleteOnTermination());
            vas.setAttachTime(va.attachTime());
            vas.setVolumeAttachmentStatus(va.stateAsString());

            results.add(vas);
        }
        return results;
    }

    public Boolean modifyVolumeType(Ec2Client ec2, String volumeId, String type) {

        ModifyVolumeRequest request = ModifyVolumeRequest.builder()
                .volumeId(volumeId)
                .volumeType(type)
                .build();

        try {
            ec2.modifyVolume(request);
            return true;
        } catch (Exception e) {
            log.error("Modify Volume Request Failed (Message: {})", e.getMessage(), e);
            return false;
        }
    }
}
