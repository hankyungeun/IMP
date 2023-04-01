package com.bootest.service;

import org.springframework.stereotype.Service;

import com.bootest.model.AwsInstanceType;
import com.bootest.type.InstanceOperationType;
import com.bootest.type.InstanceSqlType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InstanceTypePricingService {
    
    public InstanceSqlType getInstanceOperatingSystem(String instanceUsageOperation) {
        InstanceSqlType result = null;

        for (InstanceOperationType iot : InstanceOperationType.values()) {
            if (iot.getValue().equals(instanceUsageOperation)) {
                result = InstanceSqlType.valueOf(iot.toString());
            }
        }

        return result;
    }

    public Float getOdPriceByOs(InstanceSqlType os, AwsInstanceType od) {

        Float result = 0f;

        if (os != null) {
            if (os.equals(InstanceSqlType.LINUX)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.LINUX_SQL)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.LINUX_SQL_ENT)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.LINUX_SQL_WEB)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.WINDOWS)) {
                result = od.getOdWindowsPricing();
            } else if (os.equals(InstanceSqlType.WINDOWS_SQL)) {
                result = od.getOdWindowsPricing();
            } else if (os.equals(InstanceSqlType.WINDOWS_SQL_ENT)) {
                result = od.getOdWindowsPricing();
            } else if (os.equals(InstanceSqlType.WINDOWS_SQL_WEB)) {
                result = od.getOdWindowsPricing();
            } else if (os.equals(InstanceSqlType.RHEL)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.RHEL_SQL)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.RHEL_SQL_ENT)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.RHEL_SQL_WEB)) {
                result = od.getOdLinuxPricing();
            } else if (os.equals(InstanceSqlType.SUSE)) {
                result = od.getOdLinuxPricing();
            }
        }
        return result;
    }
}
