package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
public class AwsInstanceType {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(nullable = false, length = 50)
    private String instanceType;

    @Column
    private Short vcpus;

    @Column
    private Float memoryGib;

    @Column(length = 50)
    private String networkPerformance;

    @Column
    private Float odLinuxPricing;

    @Column
    private Float odWindowsPricing;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;

}
