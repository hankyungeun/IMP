package com.bootest.model;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.VolumeType;

@Data
@Entity
@Table(name = "reco_volume")
public class RecoVolume {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "account_id", nullable = false, length = 30)
    private String accountId;

    @Column(name = "volume_id", nullable = false, length = 30)
    private String volumeId;

    @Column(name = "instance_id", length = 30)
    private String instanceId;

    @Column(name = "volume_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeType volumeType;

    @Column(name = "availability_Zone", nullable = false, length = 36)
    private String availabilityZone;

    @Column(name = "create_time", length = 50)
    private String createTime;

    @Column
    private boolean encrypted;

    @Column
    private short size;

    @Column(name = "snapshot_id", length = 50)
    private String snapshotId;

    @Column(length = 30)
    private String state;

    @JsonRawValue
    @Column
    private String attachments;

    @CreationTimestamp
    @Column(name = "registered_date")
    private Date registeredDate;

    @UpdateTimestamp
    private Date updated;
}
