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
public class User {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 50)
    private String passwd;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 50)
    private String email;

    @CreationTimestamp
    private Date created;

    @UpdateTimestamp
    private Date updated;
}
