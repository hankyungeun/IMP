package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Entity
@Data
@Table(name = "reco_instance")
public class InstanceReco {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String instanceId;

    @Column(nullable = false, length = 64)
    private String instanceName;

    @Column(nullable = false, length = 36)
    private String instanceType;

    @Column(nullable = false, length = 50)
    private String launchTime;

    @Column(length = 36)
    private String availabilityZone;

    @Column(length = 50)
    private String instanceState;

    @Column(length = 30)
    private String instanceLifeCycle;

    @Column(nullable = false, length = 36)
    private String os;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;

}
