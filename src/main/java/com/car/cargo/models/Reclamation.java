package com.car.cargo.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "Reclamation")
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReclamation;

    @Column(name = "objetReclamation", nullable = false, length = 100)
    private String objetReclamation;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "telephone", nullable = false, length = 15)
    private String telephone;

    @Column(name = "dateReclamation", updatable = false)
    private LocalDateTime dateReclamation;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        this.dateReclamation = LocalDateTime.now();
    }
}
