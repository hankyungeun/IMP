package com.bootest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
// @NoArgsConstructor
public class Account {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 20)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 300)
    private String regions;

    @Column(length = 100)
    private String accessKey;

    @Column(length = 100)
    private String secretKey;

    @CreationTimestamp
    private Date created;

    @UpdateTimestamp
    private Date updated;
}
