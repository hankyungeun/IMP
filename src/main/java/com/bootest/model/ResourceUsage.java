package com.bootest.model;

import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class ResourceUsage {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String accountName;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, length = 50)
    private String resourceId;

    @Column(length = 256)
    private String resourceName;

    @Column(nullable = false, length = 50)
    private String resourceState;

    @Column(nullable = false, length = 30)
    private String lifeCycle;

    @Column(nullable = false, length = 36)
    private String imageId;

    @Column(nullable = false, length = 36)
    private String os;

    @Column(nullable = false, length = 36)
    private String instanceType;

    @Column(nullable = false)
    private short annually;

    @Column(nullable = false)
    private short monthly;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UsageDataType dataType;

    @JsonRawValue
    @Column
    private String average;

    @JsonRawValue
    @Column
    private String minimum;

    @JsonRawValue
    @Column
    private String maximum;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;
}
