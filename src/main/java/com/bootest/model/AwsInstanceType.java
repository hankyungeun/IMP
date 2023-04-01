package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "aws_instance_type")
public class AwsInstanceType {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(name = "instance_type", nullable = false, length = 50)
    private String instanceType;

    @Column
    private Short vcpus;

    @Column(name = "memory_gib")
    private Float memoryGib;

    @Column(name = "network_performance", length = 50)
    private String networkPerformance;

    @Column(name = "od_linux_pricing")
    private Float odLinuxPricing;

    @Column(name = "od_windows_pricing")
    private Float odWindowsPricing;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;

}
