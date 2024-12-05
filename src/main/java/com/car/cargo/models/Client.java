package com.car.cargo.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="Client")
public class Client {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long idClient;
	@Column(name="nomComplet",nullable=false,length=100)
	private String nomComplet;
	@Column(name="email",nullable=false,length=100)
    private String email;
	@Column(name="password",nullable=false,length=100)
    private String password;
	@Column(name="cin",nullable=false,length=100)
    private String cin;
	@Column(name="addresse",nullable=false,length=100)
    private String addresse;
	@Column(name="city",nullable=false,length=100)
    private String city;
	@Column(name="dateCreation",nullable=false,length=100)
    private LocalDateTime dateCreation;
}
