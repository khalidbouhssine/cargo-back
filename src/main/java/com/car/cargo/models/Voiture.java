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
@Table(name = "Voiture")
public class Voiture {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVoiture;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "licenceplate", nullable = false, length = 60)
    private String licenceplate;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "pricePerDay", nullable =false)
    private double pricePerDay;
    
    @Column(name = "kolometrage", nullable =false)
    private double kolometrage;
    
    @Column(name = "dateFabrication", updatable = false)
    private LocalDateTime dateFabrication;
    
    @Column(name = "imagevoiture", nullable = false)
    private Long imagevoiture;

    @PrePersist
    protected void onCreate() {
        this.dateFabrication = LocalDateTime.now();
    }
}
