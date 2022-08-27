package com.bootest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bootest.model.RecoVolume;

public interface RecoVolumeRepo extends JpaRepository<RecoVolume, String>, JpaSpecificationExecutor<RecoVolume> {

    Optional<RecoVolume> findByVolumeId(String volumeId);

}
