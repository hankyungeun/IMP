package com.bootest.model;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;

@Data
@Entity
@Table(name = "optimizer")
public class Optimizer {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String region;

    @Column(nullable = false, length = 50)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String accountName;

    @Column(nullable = false, length = 300)
    private String resourceId;

    @Column(length = 256)
    private String resourceName;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(length = 100)
    private String resourceType;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OptimizationType optimizationType;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private RecommendedAction recommendedAction;

    @Column(length = 300)
    private String recommendation;

    @Column(length = 300)
    private String optimizationReason;

    @Column(length = 50)
    private String instanceOs;

    @Column
    private Boolean optimized;

    @Column
    private Float estimatedMonthlySavings;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;
}
