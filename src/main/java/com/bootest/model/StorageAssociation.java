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
public class StorageAssociation {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 30)
    private String instanceId;

    @Column(nullable = false, length = 30)
    private String volumeId;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;
}
