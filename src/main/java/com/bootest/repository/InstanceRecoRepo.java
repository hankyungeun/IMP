package com.bootest.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.InstanceReco;

public interface InstanceRecoRepo extends JpaRepository<InstanceReco, String>, JpaSpecificationExecutor<InstanceReco> {

    Optional<InstanceReco> findByInstanceId(String instanceId);

    List<InstanceReco> findAllByAccountIdIn(Set<String> accountIds);

}
