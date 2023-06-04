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
@Table(name = "storage_association")
public class StorageAssociation {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "instance_id",nullable = false, length = 30)
    private String instanceId;

    @Column(name = "volume_id",nullable = false, length = 30)
    private String volumeId;

    @CreationTimestamp
    private Date registered;

    @UpdateTimestamp
    private Date modified;
}
