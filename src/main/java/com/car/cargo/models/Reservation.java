package com.car.cargo.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReservation;

    @ManyToOne
    @JoinColumn(name = "idVoiture", nullable = false)
    private Voiture voiture;

    @ManyToOne
    @JoinColumn(name = "startCity", nullable = false)
    private City startCity;

    @ManyToOne
    @JoinColumn(name = "endCity", nullable = false)
    private City endCity;
    
    @ManyToOne
    @JoinColumn(name = "idClient", nullable = false)
    private Client idClient; // Ajout du champ ID client

    @Column(name = "startDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "dateCreation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
