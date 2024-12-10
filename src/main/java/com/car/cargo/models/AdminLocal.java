package com.car.cargo.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="AdminLocal")
public class AdminLocal {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long IdAdminLocal;

    @Column(name = "nomComplet", nullable = false, length = 50)
    private String nomComplet;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "city", nullable = false, length = 20)
    private String city;

    @Column(name = "imageProfile", nullable = true)
    private Long imageProfile;

    @Column(name = "dateCreation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
