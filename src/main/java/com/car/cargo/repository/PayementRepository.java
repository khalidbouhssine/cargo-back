package com.car.cargo.repository;


import com.car.cargo.models.Payement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayementRepository extends JpaRepository<Payement, Long> {
}
