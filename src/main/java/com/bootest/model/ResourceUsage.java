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

    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(name = "account_name", nullable = false, length = 64)
    private String accountName;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(name = "resource_id", nullable = false, length = 50)
    private String resourceId;

    @Column(name = "resource_name",length = 256)
    private String resourceName;

    @Column(name="resource_state", nullable = false, length = 50)
    private String resourceState;

    @Column(name = "life_cycle", nullable = false, length = 30)
    private String lifeCycle;

    @Column(name = "image_id", nullable = false, length = 36)
    private String imageId;

    @Column(nullable = false, length = 36)
    private String os;

    @Column(name = "instance_type", nullable = false, length = 36)
    private String instanceType;

    @Column(nullable = false)
    private short annually;

    @Column(nullable = false)
    private short monthly;

    @Column(name = "data_type", nullable = false, length = 50)
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
