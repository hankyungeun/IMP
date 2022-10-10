package com.bootest.repository;

import com.bootest.model.ResourceUsage;
import com.bootest.type.UsageDataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ResourceUsageRepo extends JpaRepository<ResourceUsage, String>, JpaSpecificationExecutor<ResourceUsage> {

    Optional<ResourceUsage> findByResourceIdAndAnnuallyAndMonthlyAndDataType(String resourceId, short annually, short monthly, UsageDataType dataType);

    Optional<ResourceUsage> findByAccountIdAndRegionAndResourceIdAndAnnuallyAndMonthlyAndDataType(String accountId, String region, String resourceId, short annually, short monthly, UsageDataType dataType);

    List<ResourceUsage> findAllByResourceStateAndAnnuallyAndMonthlyAndDataType(String resourceState, short annually, short monthly, UsageDataType dataType);

    List<ResourceUsage> findAllByResourceIdAndAnnuallyAndMonthly(String resourceId, short annually, short monthly);

    List<ResourceUsage> findAllByResourceId(String resourceId);

}
