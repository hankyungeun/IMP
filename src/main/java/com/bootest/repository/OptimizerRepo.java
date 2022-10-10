package com.bootest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.Optimizer;
import com.bootest.type.OptimizationType;

public interface OptimizerRepo extends JpaRepository<Optimizer, String>, JpaSpecificationExecutor<Optimizer> {

    Optional<Optimizer> findByAccountIdAndResourceIdAndOptimizationType(String accountId, String resourceId,
            OptimizationType optimizationType);

    Optional<Optimizer> findByResourceIdAndOptimizationType(String resourceId, OptimizationType optimizationType);

}
