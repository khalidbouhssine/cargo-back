package com.car.cargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.car.cargo.models.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
	Client findByEmail(String email);
}
