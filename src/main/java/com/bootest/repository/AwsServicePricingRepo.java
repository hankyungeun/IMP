package com.bootest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.AwsServicePricing;
import com.bootest.type.ServiceType;

public interface AwsServicePricingRepo extends JpaRepository<AwsServicePricing, String>, JpaSpecificationExecutor<AwsServicePricing> {
    
    Optional<AwsServicePricing> findByServiceTypeAndUsageTypeAndRegion(ServiceType serviceType, String usageType, String region);

    List<AwsServicePricing> findByServiceTypeAndResourceTypeAndRegion(ServiceType serviceType, String resourceType, String regionId);

}
