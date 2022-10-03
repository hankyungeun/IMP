package com.bootest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.AwsInstanceType;

public interface AwsInstanceTypeRepo
        extends JpaRepository<AwsInstanceType, String>, JpaSpecificationExecutor<AwsInstanceType> {
    Optional<AwsInstanceType> findByInstanceType(String instanceType);

    Optional<AwsInstanceType> findByRegionAndInstanceType(String region, String instanceType);

    List<AwsInstanceType> findAllByRegion(String region);

    Boolean existsByRegion(String region);

}
