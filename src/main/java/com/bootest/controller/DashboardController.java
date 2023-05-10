package com.bootest.controller;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.OverviewResponse;
import com.bootest.dto.optimizer.GetInstanceOptResponse;
import com.bootest.model.*;
import com.bootest.repository.*;
import com.bootest.service.UnusedAndVersionOptService;
import com.bootest.type.ServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.VolumeType;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final RecoVolumeRepo recoVolumeRepo;
    private final InstanceRecoRepo instanceRecoRepo;
    private final AwsInstanceTypeRepo awsInstanceTypeRepo;
    private final AwsServicePricingRepo awsServicePricingRepo;

    @GetMapping
    public ResponseEntity<ResultObject> getOverview() {
        ResultObject result = new ResultObject();
        Float moCost = 0f;

        List<InstanceReco> irs = instanceRecoRepo.findAll();
        List<RecoVolume> vols = recoVolumeRepo.findAll();

        for (InstanceReco ir : irs) {
            String region = ir.getAvailabilityZone().substring(0, ir.getAvailabilityZone().length() - 1);

            AwsInstanceType ait = awsInstanceTypeRepo.findByRegionAndInstanceType(ir.getInstanceType(), region).orElse(null);

            if (ait != null) {
                if (ir.getOs().equalsIgnoreCase("Linux")) {
                    moCost += ait.getOdLinuxPricing() * 30;
                } else {
                    moCost += ait.getOdWindowsPricing() * 30;
                }
            }
        }

        for (RecoVolume vol : vols) {
            String region = vol.getAvailabilityZone().substring(0, vol.getAvailabilityZone().length() - 1);
            moCost += findVolCost(vol, region);
        }
        result.setResult(true);
        result.setData(new OverviewResponse(irs.size(), vols.size(), moCost));
        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }

    public Float findVolCost(RecoVolume rv, String region) {
        Float result = 0f;

        String usageType = "";
        if (rv.getVolumeType().equals(VolumeType.GP2)) {
            usageType = "VolumeUsage.gp2";
        } else if (rv.getVolumeType().equals(VolumeType.GP3)) {
            usageType = "VolumeUsage.gp3";
        } else if (rv.getVolumeType().equals(VolumeType.IO1)) {
            usageType = "VolumeUsage.piops";
        } else if (rv.getVolumeType().equals(VolumeType.IO2)) {
            usageType = "VolumeUsage.io2";
        } else if (rv.getVolumeType().equals(VolumeType.SC1)) {
            usageType = "VolumeUsage.sc1";
        } else if (rv.getVolumeType().equals(VolumeType.ST1)) {
            usageType = "VolumeUsage.st1";
        } else if (rv.getVolumeType().equals(VolumeType.STANDARD)) {
            usageType = "VolumeUsage";
        }

        AwsServicePricing volPricing = awsServicePricingRepo.findByServiceTypeAndUsageTypeAndRegion(ServiceType.VOLUME, usageType, region).orElse(null);

        if (volPricing != null) {
            result = rv.getSize() * volPricing.getPricePerUnit();
        }
        return result;
    }
}
