package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonRawValue;

import liquibase.pro.packaged.fa;
import lombok.Data;
import software.amazon.awssdk.services.ec2.model.VolumeType;

@Data
@Entity
public class RecoVolume {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String volumeId;

    @Column(length = 30)
    private String instanceId;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeType volumeType;

    @Column(nullable = false, length = 36)
    private String availabilityZone;

    @Column(length = 50)
    private String createTime;

    @Column
    private boolean encrypted;

    @Column
    private short size;

    @Column(length = 50)
    private String snapshotId;

    @Column(length = 30)
    private String state;

    @JsonRawValue
    @Column
    private String attachments;

    @CreationTimestamp
    private Date registeredDate;

    @UpdateTimestamp
    private Date updated;
}
