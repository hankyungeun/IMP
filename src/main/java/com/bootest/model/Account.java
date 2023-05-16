package com.bootest.model;

import java.io.Serializable;
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
@Table(name = "account")
// @NoArgsConstructor
public class Account implements Serializable {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "account_id", nullable = false, length = 20)
    private String accountId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 300)
    private String regions;

    @Column(name = "access_key", length = 100)
    private String accessKey;

    @Column(name = "secret_key", length = 100)
    private String secretKey;

    @CreationTimestamp
    private Date created;

    @UpdateTimestamp
    private Date updated;
}
