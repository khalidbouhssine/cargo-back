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
@Table(name = "Payement")
public class Payement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPayement;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "payementDate", nullable = false)
    private LocalDateTime payementDate;

    @Column(name = "payementMethod", nullable = false, length = 50)
    private String payementMethod;

    @PrePersist
    protected void onCreate() {
        if (this.payementDate == null) {
            this.payementDate = LocalDateTime.now(); // Set the payment date to current time if not provided
        }
    }
}
