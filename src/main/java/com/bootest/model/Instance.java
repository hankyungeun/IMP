package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Data;

@Entity
@Data

public class Instance {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 20)
    private String accountId;

    @Column(nullable = false, length = 50)
    private String accountName;

    @Column(nullable = false, length = 50)
    private String resourceId;

    @Column(nullable = false, length = 50)
    private String resourceName;

    @Column(nullable = false)
    private Short avgCpuUsage;

    @Column(nullable = false)
    private Short minCpuUsage;

    @Column(nullable = false)
    private Short maxCpuUsage;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;

}
