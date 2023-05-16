package com.bootest.model;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.bootest.type.ServiceType;
import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Data;

@Entity
@Data
@Table(name = "awsservicepricing")
public class AwsServicePricing {
    
    @Id
    @Column(nullable = false, length = 36)
    private String id;
    
    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(nullable = false, length = 300)
    private String usageType;
    
    @Column(nullable = false, length = 100)
    private String resourceType;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, length = 300)
    private String productFamily;

    @Column(length = 500)
    private String pricingDescription;

    @Column(length = 30)
    private String pricingUnit;

    @Column(length = 30)
    private String currency;
    
    @Column
    private Float pricePerUnit;

    @JsonRawValue
    @Column
    private String attributeData;
    
    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;

}
