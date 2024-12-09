package com.car.cargo.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "VerificationCode")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "clientId", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}