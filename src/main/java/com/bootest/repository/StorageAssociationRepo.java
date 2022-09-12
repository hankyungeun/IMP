package com.bootest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.StorageAssociation;

public interface StorageAssociationRepo
                extends JpaRepository<StorageAssociation, String>, JpaSpecificationExecutor<StorageAssociation> {

        Optional<StorageAssociation> findByInstanceIdAndVolumeId(String instanceId, String volumeId);

}
